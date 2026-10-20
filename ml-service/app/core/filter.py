from typing import List, Tuple, Dict, Any, Optional
from app.models.place import Place
from app.models.request import PlanRequest
from app.core.opening_hours import check_opening_hours

class CandidateFilterResult:
    def __init__(self, eligible_places: List[Place], rejection_diagnostics: Dict[str, List[str]]):
        self.eligible_places = eligible_places
        self.rejection_diagnostics = rejection_diagnostics  # reason -> list of place names

def filter_candidate_places(
    candidates: List[Place],
    request: PlanRequest
) -> CandidateFilterResult:
    """
    Applies hard constraints to candidate places:
    1. Avoid list exclusion
    2. Valid coordinates & Mumbai bounding check
    3. Accessibility / facility requirements
    4. Day-of-week closure check (e.g. closed Sundays/Mondays)
    5. Single-place cost exceeding total user budget
    """
    eligible: List[Place] = []
    diagnostics: Dict[str, List[str]] = {
        "EXCLUDED_BY_USER": [],
        "INVALID_COORDINATES": [],
        "OUTSIDE_MUMBAI_REGION": [],
        "MISSING_REQUIRED_FACILITY": [],
        "CLOSED_ON_SELECTED_DAY": [],
        "EXCEEDS_TOTAL_BUDGET": []
    }

    avoid_set = set(request.avoid_place_ids)
    accessibility_set = set(req.lower() for req in request.accessibility_needs)

    for place in candidates:
        # 1. Check avoid list
        if place.id in avoid_set:
            diagnostics["EXCLUDED_BY_USER"].append(place.name)
            continue

        # 2. Check coordinates
        if not place.has_valid_coordinates():
            diagnostics["INVALID_COORDINATES"].append(place.name)
            continue

        if not place.is_mumbai_region():
            diagnostics["OUTSIDE_MUMBAI_REGION"].append(place.name)
            continue

        # 3. Check single place cost exceeding total budget
        if place.estimatedCost > request.budget_inr:
            diagnostics["EXCEEDS_TOTAL_BUDGET"].append(
                f"{place.name} (Cost: ₹{place.estimatedCost} > Budget: ₹{request.budget_inr})"
            )
            continue

        # 4. Check facility / accessibility needs
        if accessibility_set:
            place_facs = set(f.lower() for f in place.facilities)
            # Special check for 'quiet' facility which may also be represented in quietnessLevel
            if "quiet" in accessibility_set:
                if "quiet" in place_facs or (place.quietnessLevel and place.quietnessLevel.lower() == "silent"):
                    pass
                else:
                    diagnostics["MISSING_REQUIRED_FACILITY"].append(f"{place.name} (Needs Quiet)")
                    continue

            # Check other explicit facilities (Seating, Restroom, AC, WiFi, Charging)
            missing = False
            for req_fac in accessibility_set:
                if req_fac == "quiet":
                    continue
                if req_fac not in place_facs:
                    missing = True
                    diagnostics["MISSING_REQUIRED_FACILITY"].append(f"{place.name} (Missing {req_fac})")
                    break
            if missing:
                continue

        # 5. Check day-of-week closures
        # We test with a dummy open window to verify day-level closure rules
        day_check = check_opening_hours(place.openingHours, request.date, 600, 660)
        if day_check.status_code == "CLOSED_ON_DAY":
            diagnostics["CLOSED_ON_SELECTED_DAY"].append(f"{place.name} ({day_check.warning})")
            continue

        eligible.append(place)

    return CandidateFilterResult(eligible_places=eligible, rejection_diagnostics=diagnostics)
