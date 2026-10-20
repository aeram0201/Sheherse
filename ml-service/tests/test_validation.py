import pytest
from pydantic import ValidationError
from app.models.request import PlanRequest, Coordinates, TransportMode

def test_missing_start_location_and_place_id():
    with pytest.raises(ValidationError) as exc:
        PlanRequest(
            date="2026-10-21",
            start_time="09:00",
            available_duration_minutes=300,
            budget_inr=500
        )
    assert "start_location" in str(exc.value) or "start_place_id" in str(exc.value)

def test_invalid_date_format():
    with pytest.raises(ValidationError) as exc:
        PlanRequest(
            start_location=Coordinates(latitude=18.92, longitude=72.83),
            date="21-10-2026",  # wrong format
            start_time="09:00",
            available_duration_minutes=300,
            budget_inr=500
        )
    assert "Invalid date format" in str(exc.value)

def test_invalid_start_time_format():
    with pytest.raises(ValidationError) as exc:
        PlanRequest(
            start_location=Coordinates(latitude=18.92, longitude=72.83),
            date="2026-10-21",
            start_time="25:99",  # impossible time
            available_duration_minutes=300,
            budget_inr=500
        )
    assert "Invalid start_time" in str(exc.value)

def test_duration_bounds():
    # Too short (< 30)
    with pytest.raises(ValidationError):
        PlanRequest(
            start_location=Coordinates(latitude=18.92, longitude=72.83),
            date="2026-10-21",
            start_time="09:00",
            available_duration_minutes=15,
            budget_inr=500
        )

def test_negative_budget():
    with pytest.raises(ValidationError):
        PlanRequest(
            start_location=Coordinates(latitude=18.92, longitude=72.83),
            date="2026-10-21",
            start_time="09:00",
            available_duration_minutes=300,
            budget_inr=-50
        )

def test_conflicting_preferred_and_avoid_places():
    with pytest.raises(ValidationError) as exc:
        PlanRequest(
            start_location=Coordinates(latitude=18.92, longitude=72.83),
            date="2026-10-21",
            start_time="09:00",
            available_duration_minutes=300,
            budget_inr=500,
            preferred_place_ids=["place_kyani"],
            avoid_place_ids=["place_kyani"]
        )
    assert "cannot be both preferred and avoided" in str(exc.value)

def test_api_validation_error_response(client):
    # Send empty payload to API endpoint
    response = client.post("/plan", json={})
    assert response.status_code == 422
    data = response.json()
    assert "error" in data
    assert data["error"] == "Validation Error"
    assert len(data["details"]) > 0
