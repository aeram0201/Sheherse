from app.core.planner import itinerary_planner
from app.models.request import PlanRequest, Coordinates, TransportMode
from app.models.place import Place
from app.core.filter import filter_candidate_places

def test_warnings_generated_for_estimated_prices(sample_valid_request):
    plan = itinerary_planner.plan_itinerary(sample_valid_request)
    # If any stop has priceEstimated == True, warning should be present
    has_estimated_stop = any(s.price_estimated for s in plan.stops)
    if has_estimated_stop:
        codes = [w.code for w in plan.warnings]
        assert "UNKNOWN_EXACT_PRICE" in codes

def test_warnings_generated_for_estimated_routes(sample_valid_request):
    plan = itinerary_planner.plan_itinerary(sample_valid_request)
    codes = [w.code for w in plan.warnings]
    assert "ROUTE_ESTIMATED" in codes

def test_missing_coordinates_place_rejected():
    invalid_place = Place(
        id="invalid_coords",
        name="Place With Invalid Coords",
        description="Test",
        category="Cultural",
        area="Colaba",
        address="Colaba",
        estimatedCost=0,
        priceEstimated=False,
        durationMinutes=60,
        latitude=0.0,
        longitude=0.0,  # 0,0 is invalid in Mumbai
        tags=[],
        facilities=[],
        verificationStatus="SAMPLE",
        isHiddenGem=False,
        isStudyWork=False,
        isAffordableFood=False,
        cuisineType="",
        quietnessLevel="Moderate",
        rating=4.0
    )
    req = PlanRequest(
        start_location=Coordinates(latitude=18.92, longitude=72.83),
        date="2026-10-21",
        start_time="09:00",
        available_duration_minutes=300,
        budget_inr=500
    )
    result = filter_candidate_places([invalid_place], req)
    assert len(result.eligible_places) == 0
    assert len(result.rejection_diagnostics["INVALID_COORDINATES"]) == 1

def test_empty_candidate_set_returns_infeasibility_explanation():
    # Request with impossible constraint (e.g. budget = 0 but avoid all free places)
    req = PlanRequest(
        start_location=Coordinates(latitude=18.92, longitude=72.83),
        date="2026-10-21",
        start_time="09:00",
        available_duration_minutes=120,
        budget_inr=0,
        interests=["Historical"],
        transport_mode=TransportMode.WALKING,
        accessibility_needs=["NonExistentFacility123"]  # impossible constraint
    )
    plan = itinerary_planner.plan_itinerary(req)
    assert plan.summary.is_feasible is False
    assert plan.summary.total_stops == 0
    assert len(plan.infeasibility_reasons) > 0
    assert len(plan.alternative_suggestions) > 0
