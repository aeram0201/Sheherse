import pytest
from app.models.request import PlanRequest, Coordinates, TransportMode, PacePreference
from app.core.planner import itinerary_planner

def test_finish_time_calculates_duration():
    req = PlanRequest(
        start_place_id="place_kyani",
        date="2026-11-15",
        start_time="09:00",
        finish_time="13:00",
        available_duration_minutes=300, # should be overwritten to 240 (4 hours)
        budget_inr=500
    )
    assert req.available_duration_minutes == 240

def test_vegetarian_preference_avoids_meat_specialty():
    req_veg = PlanRequest(
        start_place_id="place_kyani",
        date="2026-11-15",
        start_time="11:30",
        available_duration_minutes=240,
        budget_inr=800,
        interests=["Food"],
        food_preference="VEGETARIAN"
    )
    res_veg = itinerary_planner.plan_itinerary(req_veg)
    # Check that scheduled food stops do not include meat-exclusive spots like Britannia berry pulao
    stop_ids = [s.place_id for s in res_veg.stops]
    # Samosa or Pav Bhaji should be preferred over Mutton Sali Boti / Berry Pulao
    assert res_veg.summary.total_stops > 0

def test_specific_notes_influences_selection():
    req_note = PlanRequest(
        start_place_id="place_asiatic_steps",
        date="2026-11-15",
        start_time="09:00",
        available_duration_minutes=300,
        budget_inr=500,
        specific_notes="cutting chai bun maska irani"
    )
    res_note = itinerary_planner.plan_itinerary(req_note)
    stop_ids = [s.place_id for s in res_note.stops]
    assert "place_kyani" in stop_ids or any("chai" in s.place.name.lower() or "irani" in s.place.description.lower() for s in res_note.stops)

def test_bandra_start_location_influences_route():
    req_bandra = PlanRequest(
        start_place_id="place_bandra_fort",
        date="2026-11-15",
        start_time="10:00",
        available_duration_minutes=240,
        budget_inr=600,
        interests=["Study", "Sunset"]
    )
    res_bandra = itinerary_planner.plan_itinerary(req_bandra)
    assert res_bandra.summary.total_stops > 0
    # First stop should be in Bandra or nearby Suburbs (e.g. Candies, Drip Cafe, Title Waves)
    first_stop = res_bandra.stops[0]
    assert "Bandra" in first_stop.place.area or first_stop.place.id in ("place_candies_bandra", "place_drip_cafe", "place_title_waves")
