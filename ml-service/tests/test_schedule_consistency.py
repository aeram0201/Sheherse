from app.core.planner import itinerary_planner
from app.core.scheduler import hhmm_to_minutes

def test_schedule_timeline_arithmetic(sample_valid_request):
    plan = itinerary_planner.plan_itinerary(sample_valid_request)
    assert len(plan.stops) >= 2

    start_m = hhmm_to_minutes(sample_valid_request.start_time)
    prev_dep_m = start_m

    for i, stop in enumerate(plan.stops):
        arr_m = hhmm_to_minutes(stop.arrival_time)
        dep_m = hhmm_to_minutes(stop.departure_time)

        # Departure must equal arrival + duration
        assert dep_m == arr_m + stop.duration_minutes, (
            f"Stop {stop.place_id}: departure ({stop.departure_time}) != arrival ({stop.arrival_time}) + duration ({stop.duration_minutes})"
        )

        # Arrival must be after previous departure + travel leg
        leg = plan.travel_legs[i]
        assert arr_m >= prev_dep_m + leg.duration_minutes, (
            f"Stop {stop.place_id}: arrival ({stop.arrival_time}) too early for travel duration ({leg.duration_minutes}m)"
        )

        prev_dep_m = dep_m

    # Total elapsed duration consistency
    final_dep_m = hhmm_to_minutes(plan.stops[-1].departure_time)
    elapsed = final_dep_m - start_m
    assert elapsed <= sample_valid_request.available_duration_minutes
    assert plan.summary.total_duration_minutes == elapsed
