import re
from datetime import datetime
from enum import Enum
from typing import List, Optional
from pydantic import BaseModel, Field, model_validator

class TransportMode(str, Enum):
    WALKING = "WALKING"
    TRANSIT = "TRANSIT"
    DRIVING = "DRIVING"
    TWO_WHEELER = "TWO_WHEELER"

class PacePreference(str, Enum):
    RELAXED = "RELAXED"      # Longer dwell times, more buffer, fewer stops
    BALANCED = "BALANCED"    # Standard recommended durations
    PACKED = "PACKED"        # Shorter dwell times, maximized stop count

class Coordinates(BaseModel):
    latitude: float = Field(..., ge=-90.0, le=90.0, description="Latitude")
    longitude: float = Field(..., ge=-180.0, le=180.0, description="Longitude")

class PlanRequest(BaseModel):
    start_location: Optional[Coordinates] = Field(None, description="Starting GPS coordinates")
    start_place_id: Optional[str] = Field(None, description="Starting place ID from existing catalog")
    finish_location: Optional[Coordinates] = Field(None, description="Optional ending/destination GPS coordinates")
    finish_place_id: Optional[str] = Field(None, description="Optional ending/destination place ID")
    
    date: str = Field(..., description="Target itinerary date in YYYY-MM-DD format")
    start_time: str = Field(..., description="Starting time in 24-hour HH:MM format, e.g. 09:00")
    finish_time: Optional[str] = Field(None, description="Optional target finish time in 24-hour HH:MM format, e.g. 18:00")
    available_duration_minutes: int = Field(..., ge=30, le=1440, description="Total available minutes for the day, e.g. 360 for 6 hours")
    flexible_schedule: bool = Field(False, description="Whether user schedule is flexible")
    
    budget_inr: int = Field(..., ge=0, description="Maximum total budget in INR for the group")
    group_size: int = Field(1, ge=1, le=50, description="Number of travelers in the group")
    food_preference: Optional[str] = Field("NO_PREFERENCE", description="Food preference: VEGETARIAN, NON_VEGETARIAN, VEGAN, NO_PREFERENCE")
    include_food_costs: bool = Field(True, description="Whether estimated food costs should count against budget")
    
    interests: List[str] = Field(default_factory=list, description="Desired categories or interests, e.g. ['Historical', 'Food', 'Sunset']")
    specific_notes: Optional[str] = Field(None, description="Specific experience wish or note, e.g. 'sunset view of sea link, cutting chai'")
    
    transport_mode: TransportMode = Field(TransportMode.WALKING, description="Preferred mode of transport")
    traveler_type: Optional[str] = Field("SOLO", description="Traveler composition: SOLO, FRIENDS, FAMILY, CHILDREN")
    experience_tier: Optional[str] = Field("BALANCED", description="Experience tier: BUDGET, BALANCED, COMFORT")
    
    preferred_place_ids: List[str] = Field(default_factory=list, description="Place IDs the user explicitly wants to visit")
    avoid_place_ids: List[str] = Field(default_factory=list, description="Place IDs the user wants to exclude")
    accessibility_needs: List[str] = Field(default_factory=list, description="Required facilities/amenities, e.g. ['Seating', 'Restroom', 'Quiet', 'AC']")
    pace_preference: PacePreference = Field(PacePreference.BALANCED, description="Itinerary pacing style")
    use_ml_ranking: bool = Field(False, description="Whether to use TF-IDF ML ranking experiment instead of rule-based baseline")

    @model_validator(mode="after")
    def validate_request(self) -> "PlanRequest":
        # 1. Starting location validation
        if not self.start_location and not self.start_place_id:
            raise ValueError("Either 'start_location' coordinates or 'start_place_id' must be provided.")

        # 2. Date format validation
        try:
            datetime.strptime(self.date, "%Y-%m-%d")
        except ValueError:
            raise ValueError(f"Invalid date format '{self.date}'. Must be YYYY-MM-DD.")

        # 3. Start time validation
        if not re.match(r"^([01]\d|2[0-3]):([0-5]\d)$", self.start_time):
            raise ValueError(f"Invalid start_time '{self.start_time}'. Must be HH:MM in 24-hour format (00:00 to 23:59).")

        # 4. Finish time validation if provided
        if self.finish_time:
            if not re.match(r"^([01]\d|2[0-3]):([0-5]\d)$", self.finish_time):
                raise ValueError(f"Invalid finish_time '{self.finish_time}'. Must be HH:MM in 24-hour format.")
            # Calculate duration if finish_time given
            s_h, s_m = map(int, self.start_time.split(":"))
            f_h, f_m = map(int, self.finish_time.split(":"))
            duration = (f_h * 60 + f_m) - (s_h * 60 + s_m)
            if duration <= 0:
                raise ValueError(f"Finish time ({self.finish_time}) must be after start time ({self.start_time}).")
            self.available_duration_minutes = duration

        # 5. Check that preferred and avoid places do not intersect
        conflicting = set(self.preferred_place_ids).intersection(set(self.avoid_place_ids))
        if conflicting:
            raise ValueError(f"Places cannot be both preferred and avoided: {list(conflicting)}")

        return self
