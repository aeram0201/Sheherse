from app.core.planner import itinerary_planner
from app.models.request import PlanRequest, Coordinates, TransportMode, PacePreference

def test_plan_generation_end_to_end(sample_valid_request):
    plan = itinerary_planner.plan_itinerary(sample_valid_request)
    assert plan.itinerary_id.startswith("itin_")
    assert plan.summary.is_feasible is True
    assert plan.summary.total_stops >= 2
    assert len(plan.stops) == plan.summary.total_stops
    assert len(plan.travel_legs) == plan.summary.total_stops
    assert plan.summary.total_cost_inr <= sample_valid_request.budget_inr
    assert plan.summary.total_duration_minutes <= sample_valid_request.available_duration_minutes

    # Preferred place must be present
    stop_place_ids = [s.place_id for s in plan.stops]
    assert "place_kyani" in stop_place_ids

def test_no_duplicate_place_ids(sample_valid_request):
    plan = itinerary_planner.plan_itinerary(sample_valid_request)
    place_ids = [s.place_id for s in plan.stops]
    assert len(place_ids) == len(set(place_ids)), "Duplicate place IDs found in generated itinerary"

def test_avoid_place_ids_respected(sample_valid_request):
    # Explicitly avoid Kyani and Sassoon library
    sample_valid_request.preferred_place_ids = []
    sample_valid_request.avoid_place_ids = ["place_kyani", "place_sassoon_library"]
    plan = itinerary_planner.plan_itinerary(sample_valid_request)
    place_ids = [s.place_id for s in plan.stops]
    assert "place_kyani" not in place_ids
    assert "place_sassoon_library" not in place_ids

def test_api_post_plan_endpoint(client, sample_valid_request):
    payload = sample_valid_request.model_dump()
    response = client.post("/plan", json=payload)
    assert response.status_code == 200
    data = response.json()
    assert "itinerary_id" in data
    assert "summary" in data
    assert "stops" in data
    assert "travel_legs" in data
    assert data["summary"]["is_feasible"] is True
    assert len(data["stops"]) > 0
