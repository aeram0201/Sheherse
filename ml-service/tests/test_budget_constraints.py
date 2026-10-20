from app.core.planner import itinerary_planner
from app.models.request import PlanRequest, Coordinates, TransportMode

def test_zero_budget_only_schedules_free_places():
    req = PlanRequest(
        start_location=Coordinates(latitude=18.9328, longitude=72.8329),
        date="2026-10-21",
        start_time="10:00",
        available_duration_minutes=300,
        budget_inr=0,  # Zero budget!
        interests=["Historical", "Cultural", "Sunset"],
        transport_mode=TransportMode.WALKING
    )
    plan = itinerary_planner.plan_itinerary(req)
    assert plan.summary.total_cost_inr == 0
    for stop in plan.stops:
        assert stop.cost_inr == 0
        assert stop.place.estimatedCost == 0

def test_budget_exceeded_constraint_respected():
    req = PlanRequest(
        start_location=Coordinates(latitude=18.9328, longitude=72.8329),
        date="2026-10-21",
        start_time="10:00",
        available_duration_minutes=300,
        budget_inr=100,  # Tight budget
        interests=["Food", "Study"],
        transport_mode=TransportMode.WALKING
    )
    plan = itinerary_planner.plan_itinerary(req)
    assert plan.summary.total_cost_inr <= 100
    assert plan.summary.remaining_budget_inr >= 0

def test_total_cost_matches_stops_sum():
    req = PlanRequest(
        start_location=Coordinates(latitude=18.9328, longitude=72.8329),
        date="2026-10-21",
        start_time="09:00",
        available_duration_minutes=480,
        budget_inr=1000,
        interests=["Historical", "Food"],
        transport_mode=TransportMode.WALKING
    )
    plan = itinerary_planner.plan_itinerary(req)
    calculated_sum = sum(s.cost_inr for s in plan.stops)
    assert plan.summary.total_cost_inr == calculated_sum
