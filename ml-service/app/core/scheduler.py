from typing import List, Tuple, Optional, Dict
from app.models.place import Place
from app.models.request import PlanRequest, PacePreference
from app.models.response import ItineraryStopResponse, TravelLeg, WarningMessage
from app.core.route_service import route_service, RouteResult
from app.core.opening_hours import check_opening_hours, OpeningHoursResult

def minutes_to_hhmm(minutes: int) -> str:
    """Converts minutes from midnight to HH:MM format."""
    hours = (minutes // 60) % 24
    mins = minutes % 60
    return f"{hours:02d}:{mins:02d}"

def hhmm_to_minutes(hhmm: str) -> int:
    """Converts HH:MM string to minutes from midnight."""
    parts = hhmm.strip().split(":")
    return int(parts[0]) * 60 + int(parts[1])

def get_adjusted_duration(place: Place, pace: PacePreference) -> int:
    """Adjusts visit duration based on user pace style."""
    base = place.durationMinutes if place.durationMinutes > 0 else 60
    if pace == PacePreference.RELAXED:
        return int(round(base * 1.25))
    elif pace == PacePreference.PACKED:
        return max(int(round(base * 0.75)), 20)
    return base

def get_buffer_minutes(pace: PacePreference) -> int:
    """Buffer between departure and arrival for transitions and breathers."""
    if pace == PacePreference.RELAXED:
        return 20
    elif pace == PacePreference.PACKED:
        return 10
    return 15
