import sys
from pathlib import Path

# Add ml-service root and virtualenv site-packages to sys.path
ml_service_dir = Path(__file__).resolve().parent.parent
site_packages_dir = ml_service_dir / "venv" / "Lib" / "site-packages"

if str(site_packages_dir) not in sys.path:
    sys.path.insert(0, str(site_packages_dir))
if str(ml_service_dir) not in sys.path:
    sys.path.insert(0, str(ml_service_dir))

import pytest
from fastapi.testclient import TestClient
from app.main import app
from app.models.place import Place
from app.models.request import PlanRequest, Coordinates, TransportMode, PacePreference

@pytest.fixture(scope="session")
def client():
    return TestClient(app)

@pytest.fixture
def sample_valid_request():
    return PlanRequest(
        start_location=Coordinates(latitude=18.9328, longitude=72.8329),  # Kitab Khana / Fort
        date="2026-10-21",  # Wednesday
        start_time="09:30",
        available_duration_minutes=360,  # 6 hours
        budget_inr=500,
        interests=["Historical", "Food", "Study"],
        transport_mode=TransportMode.WALKING,
        preferred_place_ids=["place_kyani"],
        avoid_place_ids=[],
        accessibility_needs=[],
        pace_preference=PacePreference.BALANCED,
        use_ml_ranking=False
    )

@pytest.fixture
def sample_sunday_request():
    return PlanRequest(
        start_location=Coordinates(latitude=18.9328, longitude=72.8329),
        date="2026-10-25",  # Sunday
        start_time="10:00",
        available_duration_minutes=300,
        budget_inr=400,
        interests=["Food", "Cultural"],
        transport_mode=TransportMode.WALKING
    )
