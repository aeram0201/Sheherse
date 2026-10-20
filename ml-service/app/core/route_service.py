import math
import logging
import requests
from typing import Optional, Tuple
from pydantic import BaseModel
from app.config import settings

logger = logging.getLogger("sheherse.routes")

class RouteResult(BaseModel):
    distance_meters: int
    duration_minutes: int
    is_estimated: bool
    estimation_basis: str
    warning: Optional[str] = None

def haversine_distance_meters(lat1: float, lon1: float, lat2: float, lon2: float) -> float:
    """Calculates great-circle distance between two GPS points in meters."""
    R = 6371000.0  # Earth's radius in meters
    phi1 = math.radians(lat1)
    phi2 = math.radians(lat2)
    delta_phi = math.radians(lat2 - lat1)
    delta_lambda = math.radians(lon2 - lon1)

    a = (math.sin(delta_phi / 2.0) ** 2 +
         math.cos(phi1) * math.cos(phi2) * math.sin(delta_lambda / 2.0) ** 2)
    c = 2.0 * math.atan2(math.sqrt(a), math.sqrt(1.0 - a))
    return R * c

class RouteService:
    def __init__(self, api_key: Optional[str] = None):
        self.api_key = api_key if api_key is not None else settings.google_maps_api_key

    def calculate_fallback_route(
        self,
        lat1: float,
        lon1: float,
        lat2: float,
        lon2: float,
        mode: str,
        reason: str = "Fallback estimator used"
    ) -> RouteResult:
        """
        Deterministic physics-based route estimator calibrated for Mumbai's geography.
        Applies road network detour factor (~1.35x) to straight-line Haversine distance,
        and accounts for urban speeds, traffic delays, and boarding buffers.
        Never yields zero travel time unless distance is negligible (<50m).
        """
        straight_line_m = haversine_distance_meters(lat1, lon1, lat2, lon2)
        
        # If origin and destination are virtually identical (<50m)
        if straight_line_m < 50:
            return RouteResult(
                distance_meters=int(straight_line_m),
                duration_minutes=1,
                is_estimated=True,
                estimation_basis="SAME_OR_ADJACENT_LOCATION",
                warning=None
            )

        # Apply Mumbai peninsula detour factor
        detour_factor = settings.mumbai_road_detour_factor
        road_distance_m = straight_line_m * detour_factor
        road_distance_km = road_distance_m / 1000.0

        mode_upper = mode.upper()
        if mode_upper == "WALKING":
            speed_kmh = settings.speed_walk_kmh
            buffer_min = 0
        elif mode_upper == "TRANSIT":
            speed_kmh = settings.speed_transit_kmh
            buffer_min = 5  # Station access / ticketing / headway delay
        elif mode_upper == "DRIVING":
            speed_kmh = settings.speed_drive_kmh
            buffer_min = 3  # Traffic signals / parking delay
        elif mode_upper == "TWO_WHEELER":
            speed_kmh = settings.speed_drive_kmh * 1.15  # Two wheelers filter traffic slightly faster
            buffer_min = 2
        else:
            speed_kmh = settings.speed_transit_kmh
            buffer_min = 4

        travel_hours = road_distance_km / max(speed_kmh, 1.0)
        calc_minutes = int(math.ceil(travel_hours * 60.0)) + buffer_min
        # Enforce minimum 2 minutes travel time
        duration_minutes = max(calc_minutes, 2)

        basis = (
            f"HAVERSINE_MUMBAI_MODEL (detour={detour_factor:.2f}x, "
            f"speed={speed_kmh:.1f}km/h, buffer={buffer_min}m)"
        )

        return RouteResult(
            distance_meters=int(round(road_distance_m)),
            duration_minutes=duration_minutes,
            is_estimated=True,
            estimation_basis=basis,
            warning=f"Route estimated via Mumbai urban mobility model ({reason})."
        )

    def get_travel_estimate(
        self,
        lat1: float,
        lon1: float,
        lat2: float,
        lon2: float,
        mode: str = "WALKING"
    ) -> RouteResult:
        """
        Fetches travel time and distance between two locations.
        Attempts Google Maps Routes API if configured; falls back reliably on failure.
        """
        # Validate coordinates first
        if not (-90 <= lat1 <= 90 and -180 <= lon1 <= 180 and -90 <= lat2 <= 90 and -180 <= lon2 <= 180):
            return RouteResult(
                distance_meters=0,
                duration_minutes=15,
                is_estimated=True,
                estimation_basis="INVALID_COORDINATES_DEFAULT",
                warning="One or more GPS coordinates are missing or invalid; default fallback duration applied."
            )

        if not self.api_key:
            return self.calculate_fallback_route(
                lat1, lon1, lat2, lon2, mode, reason="Google Maps API key not configured"
            )

        # Map internal mode to Google Routes API TravelMode
        google_mode_map = {
            "WALKING": "WALK",
            "TRANSIT": "TRANSIT",
            "DRIVING": "DRIVE",
            "TWO_WHEELER": "TWO_WHEELER"
        }
        travel_mode = google_mode_map.get(mode.upper(), "WALK")

        url = "https://routes.googleapis.com/directions/v2:computeRoutes"
        headers = {
            "Content-Type": "application/json",
            "X-Goog-Api-Key": self.api_key,
            "X-Goog-FieldMask": "routes.distanceMeters,routes.duration"
        }
        payload = {
            "origin": {
                "location": {
                    "latLng": {"latitude": lat1, "longitude": lon1}
                }
            },
            "destination": {
                "location": {
                    "latLng": {"latitude": lat2, "longitude": lon2}
                }
            },
            "travelMode": travel_mode,
            "routingPreference": "TRAFFIC_AWARE" if travel_mode in ("DRIVE", "TWO_WHEELER") else None
        }

        try:
            resp = requests.post(url, json=payload, headers=headers, timeout=5.0)
            if resp.status_code == 200:
                data = resp.json()
                if "routes" in data and len(data["routes"]) > 0:
                    route = data["routes"][0]
                    dist_m = route.get("distanceMeters", 0)
                    duration_str = route.get("duration", "0s")
                    dur_seconds = int(duration_str.rstrip("s")) if duration_str.endswith("s") else 0
                    dur_minutes = max(int(math.ceil(dur_seconds / 60.0)), 1)
                    return RouteResult(
                        distance_meters=dist_m,
                        duration_minutes=dur_minutes,
                        is_estimated=False,
                        estimation_basis="GOOGLE_MAPS_ROUTES_API",
                        warning=None
                    )
                else:
                    return self.calculate_fallback_route(
                        lat1, lon1, lat2, lon2, mode, reason="Google Routes API returned no routes"
                    )
            elif resp.status_code == 429:
                return self.calculate_fallback_route(
                    lat1, lon1, lat2, lon2, mode, reason="Google Maps API rate limit / quota exceeded"
                )
            else:
                return self.calculate_fallback_route(
                    lat1, lon1, lat2, lon2, mode, reason=f"Google Routes API HTTP {resp.status_code}"
                )
        except requests.Timeout:
            return self.calculate_fallback_route(
                lat1, lon1, lat2, lon2, mode, reason="Google Routes API network request timed out"
            )
        except Exception as e:
            return self.calculate_fallback_route(
                lat1, lon1, lat2, lon2, mode, reason=f"Google Routes API exception: {str(e)}"
            )

route_service = RouteService()
