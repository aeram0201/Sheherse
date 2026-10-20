from typing import List, Optional, Dict, Any
from pydantic import BaseModel, Field
from app.models.place import Place

class TransitStep(BaseModel):
    step_type: str = Field("WALK", description="WALK, LOCAL_TRAIN, METRO, TAXI, or BUS")
    instruction: str = Field(..., description="Actionable step instruction")
    duration_minutes: int = Field(..., description="Minutes for this step")
    distance_meters: int = Field(0, description="Meters for this step")
    line_name: Optional[str] = Field(None, description="Transit line name, e.g. Western Line")
    boarding_stop: Optional[str] = Field(None, description="Boarding station/stop name")
    exit_stop: Optional[str] = Field(None, description="Alighting station/stop name")
    num_stops: Optional[int] = Field(None, description="Number of intermediate stops")
    fare_inr: Optional[int] = Field(None, description="Step fare in INR if applicable")
    is_fare_estimated: bool = Field(False, description="True if fare is an estimate")
    source: Optional[str] = Field(None, description="Source of fare or route info")

class TravelLeg(BaseModel):
    from_title: str = Field(..., description="Name of origin stop or 'Starting Point'")
    to_title: str = Field(..., description="Name of destination stop")
    from_place_id: Optional[str] = Field(None, description="Origin place ID if applicable")
    to_place_id: str = Field(..., description="Destination place ID")
    mode: str = Field(..., description="Travel mode (WALKING, TRANSIT, DRIVING, TWO_WHEELER)")
    distance_meters: int = Field(..., description="Distance in meters")
    duration_minutes: int = Field(..., description="Travel time in minutes")
    is_estimated: bool = Field(..., description="True if estimated via deterministic physics model rather than live Routes API")
    estimation_basis: str = Field(..., description="Method used for route calculation")
    route_warning: Optional[str] = Field(None, description="Warning if route service encountered an issue")
    recommended_mode: str = Field("WALKING", description="Recommended transport mode (WALKING, LOCAL_TRAIN, METRO, TAXI_CAB, AUTO_RICKSHAW)")
    transit_available: bool = Field(True, description="True if verified public transit instructions exist for this leg")
    transit_summary: str = Field("Direct walk", description="Concise transit instruction or status explanation")
    boarding_stop: Optional[str] = Field(None, description="Boarding station or stop if applicable")
    exit_stop: Optional[str] = Field(None, description="Alighting station or stop if applicable")
    line_or_route: Optional[str] = Field(None, description="Suburban railway line, metro line, or bus number")
    walking_distance_meters: int = Field(0, description="Total walking distance in meters for this leg")
    walking_duration_minutes: int = Field(0, description="Total walking time in minutes for this leg")
    fare_inr: Optional[int] = Field(0, description="Sourced or estimated fare in INR; None if unknown")
    fare_is_estimated: bool = Field(False, description="True if fare is estimated rather than exact published fare")
    fare_source: Optional[str] = Field(None, description="Sourced tariff origin or estimator note")
    steps: List[TransitStep] = Field(default_factory=list, description="Step-by-step navigation instructions")

class ItineraryStopResponse(BaseModel):
    stop_id: str = Field(..., description="Unique ID for this stop instance")
    place_id: str = Field(..., description="Referenced Firestore place ID")
    order_index: int = Field(..., description="0-based sequence index")
    place: Place = Field(..., description="Complete place details matching SheherSe Android Place model")
    arrival_time: str = Field(..., description="Planned arrival time in 24-hr HH:MM format")
    departure_time: str = Field(..., description="Planned departure time in 24-hr HH:MM format")
    duration_minutes: int = Field(..., description="Minutes allocated at this place")
    cost_inr: int = Field(..., description="Known or estimated cost in INR")
    price_estimated: bool = Field(..., description="True if price is an estimate")
    cost_note: str = Field(..., description="Explanation of cost (e.g. Free, Exact entry fee, Estimated snack budget)")
    opening_status: str = Field(..., description="CONFIRMED_OPEN, HOURS_UNKNOWN, or CLOSED_WARNING")
    recommendation_reason: str = Field(..., description="Why this stop was selected for the user")
    custom_note: Optional[str] = Field(None, description="Insider or scheduling note for the stop")

class CostBreakdown(BaseModel):
    entry_fees_inr: int = Field(0, description="Attraction entry fees total")
    food_cost_inr: int = Field(0, description="Food and beverage total")
    transport_fare_inr: int = Field(0, description="Transit and transport fare total")
    total_cost_inr: int = Field(0, description="Sum of all costs")
    has_unknown_costs: bool = Field(False, description="True if any stop has unknown price")
    cost_completeness_note: str = Field("All fees sourced", description="Completeness explanation")

class ItinerarySummary(BaseModel):
    total_stops: int = Field(..., description="Number of scheduled stops")
    total_duration_minutes: int = Field(..., description="Total elapsed minutes including travel, visits and buffers")
    total_travel_minutes: int = Field(..., description="Sum of travel minutes between stops")
    total_visit_minutes: int = Field(..., description="Sum of visit minutes across all stops")
    total_cost_inr: int = Field(..., description="Sum of known and estimated costs in INR")
    cost_is_fully_known: bool = Field(..., description="False if any stop has unknown or estimated price")
    budget_inr: int = Field(..., description="User's initial budget")
    remaining_budget_inr: int = Field(..., description="Budget left after planned stops")
    is_feasible: bool = Field(..., description="True if itinerary fits within user time and budget constraints")
    cost_breakdown: Optional[CostBreakdown] = Field(None, description="Detailed category-wise cost breakdown")

class WarningMessage(BaseModel):
    code: str = Field(..., description="Standardized error/warning code")
    message: str = Field(..., description="Human-readable warning explanation")
    place_id: Optional[str] = Field(None, description="Place ID associated with warning if applicable")
    details: Optional[Dict[str, Any]] = Field(None, description="Additional debug context")

class PlanResponse(BaseModel):
    itinerary_id: str = Field(..., description="Generated itinerary ID")
    title: str = Field(..., description="Descriptive title of the itinerary")
    summary: ItinerarySummary = Field(..., description="Key metrics and totals for the day plan")
    stops: List[ItineraryStopResponse] = Field(default_factory=list, description="Ordered sequence of stops")
    travel_legs: List[TravelLeg] = Field(default_factory=list, description="Travel legs connecting stops")
    warnings: List[WarningMessage] = Field(default_factory=list, description="Structured warnings regarding hours, routes or prices")
    planning_rationale: str = Field(..., description="Explanation of geographic grouping, timing and category selection")
    infeasibility_reasons: List[str] = Field(default_factory=list, description="Reasons if plan is unfeasible or partial")
    alternative_suggestions: List[str] = Field(default_factory=list, description="Actionable recommendations to improve feasibility")

class HealthResponse(BaseModel):
    status: str
    service: str
    version: str
    routes_api_status: str
    data_source: str
    total_places_loaded: int
