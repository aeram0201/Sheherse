import pytest
from app.core.transit_planner import (
    transit_planner,
    WESTERN_LINE_STATIONS,
    calculate_local_train_fare,
    calculate_mumbai_taxi_fare
)
from app.models.place import Place, MenuItem
from app.models.request import PlanRequest, TransportMode
from app.core.planner import itinerary_planner

def test_short_distance_walking_transit():
    # Two close spots in South Bombay (e.g. Asiatic Steps to Kitab Khana, ~600m)
    t = transit_planner.plan_leg_transit(
        18.9298, 72.8333,
        18.9324, 72.8315,
        "Asiatic Library", "Kitab Khana",
        requested_mode="WALKING"
    )
    assert t["recommended_mode"] == "WALKING"
    assert t["fare_inr"] == 0
    assert t["transit_available"] is True
    assert "walk" in t["transit_summary"].lower()
    assert len(t["steps"]) >= 1

def test_western_line_suburban_transit():
    # Churchgate area to Bandra area (~14 km apart along Western Line)
    # Churchgate: 18.9322, 72.8264
    # Bandra: 19.0544, 72.8402
    t = transit_planner.plan_leg_transit(
        18.9320, 72.8260,
        19.0540, 72.8400,
        "Colaba Start", "Bandra Stop",
        requested_mode="TRANSIT"
    )
    assert t["recommended_mode"] == "LOCAL_TRAIN"
    assert t["line_or_route"] == "Western Line"
    assert t["boarding_stop"] == "Churchgate Railway Station"
    assert t["exit_stop"] == "Bandra Railway Station"
    assert t["fare_inr"] == 10  # 11-20 km = 10 INR
    assert t["fare_is_estimated"] is False
    assert t["transit_available"] is True
    assert len(t["steps"]) == 3  # walk to stn, train ride, walk to dest

def test_unavailable_transit_honest_declaration():
    # Cross-town points without single direct line (e.g. Malabar Hill 18.9452, 72.7937 to Mazgaon/Byculla East)
    # Not near stations on same line
    t = transit_planner.plan_leg_transit(
        18.9452, 72.7937,
        18.9680, 72.8450,
        "Banganga Tank", "Eastern Dock",
        requested_mode="DRIVING"
    )
    assert t["transit_available"] is False
    assert "unavailable" in t["transit_summary"].lower()
    assert t["recommended_mode"] in ("TAXI_CAB", "AUTO_RICKSHAW")
    assert t["fare_inr"] > 0
    assert t["fare_is_estimated"] is True
    assert "MMRTA" in t["fare_source"]

def test_plan_response_includes_cost_breakdown():
    req = PlanRequest(
        date="2026-11-15",
        start_time="09:00",
        available_duration_minutes=360,
        budget_inr=500,
        start_location={"latitude": 18.9220, "longitude": 72.8347},
        interests=["Historical"]
    )
    resp = itinerary_planner.plan_itinerary(req)
    assert resp.summary.cost_breakdown is not None
    cb = resp.summary.cost_breakdown
    assert cb.total_cost_inr >= 0
    assert cb.entry_fees_inr >= 0
    assert cb.transport_fare_inr >= 0
    assert len(cb.cost_completeness_note) > 0
    # Every travel leg has transit details
    for leg in resp.travel_legs:
        assert leg.recommended_mode in ("WALKING", "LOCAL_TRAIN", "METRO", "TAXI_CAB", "AUTO_RICKSHAW")
        assert leg.transit_summary is not None
        assert len(leg.steps) >= 1

def test_place_menu_item_model():
    item = MenuItem(
        name="Bun Maska & Irani Chai",
        price_inr=60,
        dietary_type="VEGETARIAN",
        description="Freshly baked bun with salted butter",
        recommendation_reason="Legendary combo since 1904",
        source_provenance="Kyani & Co. In-House Menu (Verified 2026)"
    )
    p = Place(
        id="test_food_place",
        name="Test Cafe",
        description="Test Cafe",
        category="Food",
        area="South Mumbai",
        address="123 Street",
        latitude=18.93,
        longitude=72.83,
        menu_items=[item]
    )
    assert len(p.menu_items) == 1
    assert p.menu_items[0].name == "Bun Maska & Irani Chai"
    assert p.menu_items[0].price_inr == 60
    assert p.menu_items[0].dietary_type == "VEGETARIAN"
