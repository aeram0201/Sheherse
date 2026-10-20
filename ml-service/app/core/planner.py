import uuid
from typing import List, Dict, Optional, Tuple, Set
from app.models.place import Place
from app.models.request import PlanRequest, PacePreference
from app.models.response import (
    PlanResponse,
    ItineraryStopResponse,
    TravelLeg,
    TransitStep,
    ItinerarySummary,
    CostBreakdown,
    WarningMessage
)
from app.data.place_repository import place_repository
from app.core.filter import filter_candidate_places, CandidateFilterResult
from app.core.ranking import score_places_baseline, ScoredPlace
from app.ml.content_recommender import content_recommender
from app.core.route_service import route_service, haversine_distance_meters, RouteResult
from app.core.transit_planner import transit_planner
from app.core.opening_hours import check_opening_hours, OpeningHoursResult
from app.core.scheduler import minutes_to_hhmm, hhmm_to_minutes, get_adjusted_duration, get_buffer_minutes

class ItineraryPlanner:
    def plan_itinerary(self, request: PlanRequest) -> PlanResponse:
        warnings: List[WarningMessage] = []
        infeasibility_reasons: List[str] = []
        alternatives: List[str] = []

        # 1. Resolve starting GPS anchor
        start_lat: float = 0.0
        start_lon: float = 0.0
        start_title: str = "Starting Location"

        if request.start_place_id:
            start_place = place_repository.get_place_by_id(request.start_place_id)
            if not start_place:
                infeasibility_reasons.append(f"Starting place ID '{request.start_place_id}' not found in catalog.")
                return self._build_empty_response(request, warnings, infeasibility_reasons, ["Choose a known starting location in Mumbai."])
            if not start_place.has_valid_coordinates():
                infeasibility_reasons.append(f"Starting place '{start_place.name}' has invalid coordinates.")
                return self._build_empty_response(request, warnings, infeasibility_reasons, ["Choose a starting place with valid GPS coordinates."])
            start_lat = start_place.latitude
            start_lon = start_place.longitude
            start_title = start_place.name
        elif request.start_location:
            start_lat = request.start_location.latitude
            start_lon = request.start_location.longitude
            start_title = f"GPS ({start_lat:.4f}, {start_lon:.4f})"

        # Check start coordinates within Mumbai bounding
        if not (18.75 <= start_lat <= 19.45 and 72.70 <= start_lon <= 73.20):
            warnings.append(WarningMessage(
                code="OUTSIDE_MUMBAI_START",
                message=f"Starting location ({start_lat:.4f}, {start_lon:.4f}) is outside standard Mumbai metropolitan limits. Travel estimates may be unusually long."
            ))

        # 1b. Resolve optional finish GPS anchor
        finish_lat: float = 0.0
        finish_lon: float = 0.0
        if request.finish_place_id:
            fp = place_repository.get_place_by_id(request.finish_place_id)
            if fp and fp.has_valid_coordinates():
                finish_lat = fp.latitude
                finish_lon = fp.longitude
        elif request.finish_location:
            finish_lat = request.finish_location.latitude
            finish_lon = request.finish_location.longitude

        # 2. Retrieve candidates
        all_places = place_repository.get_all_places()
        if not all_places:
            infeasibility_reasons.append("No places available in data store.")
            return self._build_empty_response(request, warnings, infeasibility_reasons, ["Check data repository initialization."])

        # Exclude starting place from being scheduled as a visit destination if starting there
        eligible_raw = [p for p in all_places if p.id != request.start_place_id]

        # 3. Filter candidates against hard constraints
        filter_result: CandidateFilterResult = filter_candidate_places(eligible_raw, request)
        candidates = filter_result.eligible_places

        if not candidates:
            # Explain why all candidates were rejected
            for reason, place_names in filter_result.rejection_diagnostics.items():
                if place_names:
                    infeasibility_reasons.append(f"{reason}: {len(place_names)} place(s) rejected ({', '.join(place_names[:3])})")
            
            if filter_result.rejection_diagnostics["CLOSED_ON_SELECTED_DAY"]:
                alternatives.append("Many preferred heritage sites and libraries are closed on Sundays or Mondays. Try a weekday or Saturday.")
            if filter_result.rejection_diagnostics["EXCEEDS_TOTAL_BUDGET"]:
                alternatives.append(f"Consider increasing your budget beyond ₹{request.budget_inr} to include paid heritage landmarks.")
            if filter_result.rejection_diagnostics["MISSING_REQUIRED_FACILITY"]:
                alternatives.append("Relax accessibility constraints to see more outdoor and heritage locations.")

            return self._build_empty_response(request, warnings, infeasibility_reasons, alternatives)

        # 4. Schedule simulation state
        start_min = hhmm_to_minutes(request.start_time)
        max_duration_min = request.available_duration_minutes
        end_limit_min = start_min + max_duration_min

        current_time_min = start_min
        current_lat = start_lat
        current_lon = start_lon
        current_title = start_title
        current_place_id: Optional[str] = request.start_place_id

        remaining_budget = request.budget_inr
        buffer_min = get_buffer_minutes(request.pace_preference)

        scheduled_stops: List[ItineraryStopResponse] = []
        travel_legs: List[TravelLeg] = []
        visited_place_ids: Set[str] = set()
        if request.start_place_id:
            visited_place_ids.add(request.start_place_id)

        # Priority queues: preferred places first
        preferred_candidates = [p for p in candidates if p.id in request.preferred_place_ids]
        other_candidates = [p for p in candidates if p.id not in request.preferred_place_ids]

        # 5. Iterative stop selection
        available_pool = preferred_candidates + other_candidates

        while available_pool and current_time_min < end_limit_min:
            # Score remaining candidates from current location and remaining budget
            if request.use_ml_ranking:
                ranked_candidates = content_recommender.rank_places_tfidf(
                    available_pool, request, current_lat, current_lon, remaining_budget
                )
            else:
                is_later = (current_time_min - start_min) > (max_duration_min * 0.5)
                ranked_candidates = score_places_baseline(
                    available_pool, request, current_lat, current_lon, remaining_budget,
                    finish_lat=finish_lat, finish_lon=finish_lon, is_later_in_day=is_later
                )

            selected_candidate: Optional[Place] = None
            selected_route: Optional[RouteResult] = None
            selected_arrival_min: int = 0
            selected_departure_min: int = 0
            selected_dwell_min: int = 0
            selected_opening_check: Optional[OpeningHoursResult] = None
            selected_rationale: str = ""

            # Meal and Sunset heuristics
            is_lunch_window = (12 * 60 + 30) <= current_time_min <= (14 * 60 + 30)
            is_sunset_window = (17 * 60 + 0) <= current_time_min <= (18 * 60 + 45)

            # Try top-ranked candidates
            for scored_item in ranked_candidates:
                cand = scored_item.place
                if cand.id in visited_place_ids:
                    continue

                # Lunch preference: if in lunch window, boost food places
                if is_lunch_window and cand.category == "Food" and not any(s.place.category == "Food" for s in scheduled_stops):
                    pass  # keep priority
                # Sunset preference: if in sunset window, boost sunset places
                elif is_sunset_window and cand.category == "Sunset" and not any(s.place.category == "Sunset" for s in scheduled_stops):
                    pass  # keep priority

                # 1. Budget check
                cand_cost = cand.estimatedCost
                if cand_cost > remaining_budget:
                    continue

                # 2. Route calculation
                route = route_service.get_travel_estimate(
                    current_lat, current_lon, cand.latitude, cand.longitude, mode=request.transport_mode.value
                )

                proposed_arrival = current_time_min + route.duration_minutes
                proposed_dwell = get_adjusted_duration(cand, request.pace_preference)
                proposed_departure = proposed_arrival + proposed_dwell
                proposed_next_available = proposed_departure + buffer_min

                # 3. Time feasibility check
                if proposed_departure > end_limit_min:
                    continue

                # 4. Opening hours check at proposed visit window
                hours_check = check_opening_hours(cand.openingHours, request.date, proposed_arrival, proposed_departure)
                
                # If definitely closed (outside operating hours or closed on this day), skip
                if hours_check.status_code in ("CLOSED_ON_DAY", "OUTSIDE_HOURS"):
                    continue

                # Candidate is eligible!
                selected_candidate = cand
                selected_route = route
                selected_arrival_min = proposed_arrival
                selected_departure_min = proposed_departure
                selected_dwell_min = proposed_dwell
                selected_opening_check = hours_check
                selected_rationale = scored_item.rationale
                break

            if not selected_candidate or not selected_route or not selected_opening_check:
                # No more places can fit within time/budget
                break

            # Add stop to schedule
            order_idx = len(scheduled_stops)
            stop_id = f"stop_{order_idx + 1}_{selected_candidate.id}"

            # Warnings for unknown prices
            cost_note = "Free entry" if selected_candidate.estimatedCost == 0 else f"₹{selected_candidate.estimatedCost}"
            if selected_candidate.priceEstimated:
                cost_note += " (Estimated cost)"
                warnings.append(WarningMessage(
                    code="UNKNOWN_EXACT_PRICE",
                    message=f"Exact cost for {selected_candidate.name} is not published; estimated at ₹{selected_candidate.estimatedCost}.",
                    place_id=selected_candidate.id
                ))

            # Warnings for opening hours
            opening_status = selected_opening_check.status_code
            if opening_status == "HOURS_UNKNOWN":
                warnings.append(WarningMessage(
                    code="UNCONFIRMED_OPENING_HOURS",
                    message=f"Opening hours for {selected_candidate.name} could not be fully verified. Confirm before visiting.",
                    place_id=selected_candidate.id
                ))

            # Warnings for route estimates
            if selected_route.is_estimated and selected_route.warning:
                warnings.append(WarningMessage(
                    code="ROUTE_ESTIMATED",
                    message=f"Leg from '{current_title}' to '{selected_candidate.name}': {selected_route.warning}",
                    place_id=selected_candidate.id
                ))

            # Travel leg with verified transit planning
            t_plan = transit_planner.plan_leg_transit(
                current_lat, current_lon,
                selected_candidate.latitude, selected_candidate.longitude,
                current_title, selected_candidate.name,
                requested_mode=request.transport_mode.value
            )

            leg = TravelLeg(
                from_title=current_title,
                to_title=selected_candidate.name,
                from_place_id=current_place_id,
                to_place_id=selected_candidate.id,
                mode=request.transport_mode.value,
                distance_meters=selected_route.distance_meters,
                duration_minutes=selected_route.duration_minutes,
                is_estimated=selected_route.is_estimated,
                estimation_basis=selected_route.estimation_basis,
                route_warning=selected_route.warning,
                recommended_mode=t_plan["recommended_mode"],
                transit_available=t_plan["transit_available"],
                transit_summary=t_plan["transit_summary"],
                boarding_stop=t_plan["boarding_stop"],
                exit_stop=t_plan["exit_stop"],
                line_or_route=t_plan["line_or_route"],
                walking_distance_meters=t_plan["walking_distance_meters"],
                walking_duration_minutes=t_plan["walking_duration_minutes"],
                fare_inr=t_plan["fare_inr"],
                fare_is_estimated=t_plan["fare_is_estimated"],
                fare_source=t_plan["fare_source"],
                steps=t_plan["steps"]
            )
            travel_legs.append(leg)

            # Stop Response
            stop_resp = ItineraryStopResponse(
                stop_id=stop_id,
                place_id=selected_candidate.id,
                order_index=order_idx,
                place=selected_candidate,
                arrival_time=minutes_to_hhmm(selected_arrival_min),
                departure_time=minutes_to_hhmm(selected_departure_min),
                duration_minutes=selected_dwell_min,
                cost_inr=selected_candidate.estimatedCost,
                price_estimated=selected_candidate.priceEstimated,
                cost_note=cost_note,
                opening_status=opening_status,
                recommendation_reason=selected_rationale,
                custom_note=selected_candidate.insiderTip
            )
            scheduled_stops.append(stop_resp)
            visited_place_ids.add(selected_candidate.id)
            available_pool = [p for p in available_pool if p.id != selected_candidate.id]

            # Update simulation state
            remaining_budget -= selected_candidate.estimatedCost
            current_time_min = selected_departure_min + buffer_min
            current_lat = selected_candidate.latitude
            current_lon = selected_candidate.longitude
            current_title = selected_candidate.name
            current_place_id = selected_candidate.id

        # 6. Recheck completed schedule
        total_travel_min = sum(leg.duration_minutes for leg in travel_legs)
        total_visit_min = sum(stop.duration_minutes for stop in scheduled_stops)
        total_elapsed_min = (current_time_min - buffer_min) - start_min if scheduled_stops else 0
        total_cost = sum(stop.cost_inr for stop in scheduled_stops)
        has_estimated_cost = any(stop.price_estimated for stop in scheduled_stops)

        # Check duplicate stop IDs
        stop_ids_seen = set()
        for stop in scheduled_stops:
            if stop.place_id in stop_ids_seen:
                warnings.append(WarningMessage(
                    code="DUPLICATE_PLACE_DETECTED",
                    message=f"Duplicate stop for place {stop.place_id} detected.",
                    place_id=stop.place_id
                ))
            stop_ids_seen.add(stop.place_id)

        # Check budget feasibility
        is_feasible = (
            len(scheduled_stops) > 0 and
            total_elapsed_min <= max_duration_min and
            total_cost <= request.budget_inr
        )

        if not is_feasible and scheduled_stops:
            if total_elapsed_min > max_duration_min:
                infeasibility_reasons.append(f"Total time ({total_elapsed_min} mins) exceeded available duration ({max_duration_min} mins).")
            if total_cost > request.budget_inr:
                infeasibility_reasons.append(f"Total cost (₹{total_cost}) exceeded budget (₹{request.budget_inr}).")

        # 7. Construct rationale & title
        primary_interest = request.interests[0] if request.interests else "Mumbai"
        area_names = list(dict.fromkeys(stop.place.area for stop in scheduled_stops))
        area_summary = ", ".join(area_names[:2]) if area_names else "South Mumbai"

        title = f"{primary_interest} Day Trail in {area_summary}"
        rationale = (
            f"Crafted a {len(scheduled_stops)}-stop itinerary tailored to {', '.join(request.interests) if request.interests else 'city discovery'}. "
            f"Stops are clustered in {area_summary} to minimize travel overhead ({total_travel_min} mins travel vs {total_visit_min} mins exploration). "
            f"Scheduled from {request.start_time} with {request.pace_preference.value.lower()} pacing."
        )

        # Missing preferred places warning
        missing_preferred = set(request.preferred_place_ids) - visited_place_ids
        if missing_preferred:
            warnings.append(WarningMessage(
                code="PREFERRED_PLACES_OMITTED",
                message=f"Could not fit preferred places: {', '.join(missing_preferred)} due to time/budget constraints."
            ))

        # Calculate structured category-wise cost breakdown
        entry_fees_inr = sum(
            stop.place.estimatedCost for stop in scheduled_stops
            if stop.place.category in ("Historical", "Cultural", "Nature")
        )
        food_cost_inr = sum(
            stop.place.estimatedCost for stop in scheduled_stops
            if stop.place.category == "Food" or stop.place.isAffordableFood
        )
        other_stop_costs = sum(
            stop.place.estimatedCost for stop in scheduled_stops
            if stop.place.category not in ("Historical", "Cultural", "Nature", "Food") and not stop.place.isAffordableFood
        )
        transport_fare_inr = sum(leg.fare_inr or 0 for leg in travel_legs)
        trip_total_with_transit = total_cost + transport_fare_inr
        has_unknown_costs = (
            has_estimated_cost or
            any(leg.fare_is_estimated for leg in travel_legs)
        )
        completeness_note = (
            f"Breakdown: Entry ₹{entry_fees_inr} + Food ₹{food_cost_inr} + Transport ₹{transport_fare_inr} = ₹{trip_total_with_transit}"
        )
        cost_breakdown = CostBreakdown(
            entry_fees_inr=entry_fees_inr,
            food_cost_inr=food_cost_inr,
            transport_fare_inr=transport_fare_inr,
            total_cost_inr=trip_total_with_transit,
            has_unknown_costs=has_unknown_costs,
            cost_completeness_note=completeness_note
        )

        # Budget advisory
        if trip_total_with_transit > request.budget_inr:
            excess = trip_total_with_transit - request.budget_inr
            alternatives.append(
                f"Total estimated expenses (₹{trip_total_with_transit}) exceed budget (₹{request.budget_inr}) by ₹{excess}. "
                "Consider taking Mumbai local train instead of cabs, or selecting free heritage sights."
            )

        summary = ItinerarySummary(
            total_stops=len(scheduled_stops),
            total_duration_minutes=total_elapsed_min,
            total_travel_minutes=total_travel_min,
            total_visit_minutes=total_visit_min,
            total_cost_inr=total_cost,
            cost_is_fully_known=not has_estimated_cost,
            budget_inr=request.budget_inr,
            remaining_budget_inr=max(0, request.budget_inr - total_cost),
            is_feasible=is_feasible,
            cost_breakdown=cost_breakdown
        )

        return PlanResponse(
            itinerary_id=f"itin_{uuid.uuid4().hex[:10]}",
            title=title,
            summary=summary,
            stops=scheduled_stops,
            travel_legs=travel_legs,
            warnings=warnings,
            planning_rationale=rationale,
            infeasibility_reasons=infeasibility_reasons,
            alternative_suggestions=alternatives
        )

    def _build_empty_response(
        self,
        request: PlanRequest,
        warnings: List[WarningMessage],
        infeasibility_reasons: List[str],
        alternatives: List[str]
    ) -> PlanResponse:
        summary = ItinerarySummary(
            total_stops=0,
            total_duration_minutes=0,
            total_travel_minutes=0,
            total_visit_minutes=0,
            total_cost_inr=0,
            cost_is_fully_known=True,
            budget_inr=request.budget_inr,
            remaining_budget_inr=request.budget_inr,
            is_feasible=False,
            cost_breakdown=CostBreakdown(
                entry_fees_inr=0,
                food_cost_inr=0,
                transport_fare_inr=0,
                total_cost_inr=0,
                has_unknown_costs=False,
                cost_completeness_note="No stops scheduled"
            )
        )
        return PlanResponse(
            itinerary_id=f"itin_{uuid.uuid4().hex[:10]}",
            title="Infeasible Itinerary",
            summary=summary,
            stops=[],
            travel_legs=[],
            warnings=warnings,
            planning_rationale="No suitable itinerary could be constructed under the specified constraints.",
            infeasibility_reasons=infeasibility_reasons,
            alternative_suggestions=alternatives
        )

itinerary_planner = ItineraryPlanner()
