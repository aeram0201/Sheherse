import logging
from typing import Dict, Any, Optional
from fastapi import FastAPI, HTTPException, Header, Depends, status, Request
from fastapi.responses import JSONResponse
from fastapi.exceptions import RequestValidationError
from fastapi.middleware.cors import CORSMiddleware

from app.config import settings
from app.models.request import PlanRequest
from app.models.response import PlanResponse, HealthResponse
from app.data.place_repository import place_repository
from app.data.firestore_client import verify_firebase_token
from app.core.planner import itinerary_planner
from app.ml.content_recommender import content_recommender

# Logging configuration
logging.basicConfig(
    level=logging.INFO if not settings.debug else logging.DEBUG,
    format="%(asctime)s [%(levelname)s] %(name)s: %(message)s"
)
logger = logging.getLogger("sheherse.api")

app = FastAPI(
    title="SheherSe AI Itinerary Planning Engine",
    description=(
        "Personalised, geographically sensible, time-aware, and budget-aware "
        "day itinerary planning engine for Mumbai discovery. Built for integration with the "
        "SheherSe Android application and Firebase backend."
    ),
    version="1.0.0",
    docs_url="/docs",
    redoc_url="/redoc"
)

# Enable CORS for Android emulator, local dev and web clients
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Custom validation exception handler
@app.exception_handler(RequestValidationError)
async def validation_exception_handler(request: Request, exc: RequestValidationError):
    errors = []
    for err in exc.errors():
        field = " -> ".join(str(loc) for loc in err.get("loc", []))
        msg = err.get("msg", "Validation error")
        errors.append({"field": field, "message": msg})
    return JSONResponse(
        status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
        content={
            "error": "Validation Error",
            "message": "The itinerary request parameters failed validation.",
            "details": errors
        }
    )

# Security dependency
async def optional_firebase_auth(authorization: Optional[str] = Header(None)) -> Optional[dict]:
    """
    Validates Firebase Bearer ID Token if REQUIRE_FIREBASE_AUTH is enabled,
    or extracts user identity if token is provided.
    """
    if not authorization:
        if settings.require_firebase_auth:
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Authorization header with Bearer token is required."
            )
        return None

    if not authorization.startswith("Bearer "):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid authorization scheme. Must be 'Bearer <token>'."
        )

    token = authorization.split("Bearer ", 1)[1].strip()
    decoded_user = verify_firebase_token(token)

    if not decoded_user and settings.require_firebase_auth:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid or expired Firebase authentication token."
        )

    return decoded_user

@app.get("/health", response_model=HealthResponse, tags=["System"])
async def health_check():
    """
    Health check endpoint reporting service status, Google Maps Routes API status,
    and the active data source (Firestore or local seed dataset).
    """
    routes_status = "CONFIGURED (Live API)" if settings.google_maps_api_key else "FALLBACK (Mumbai mobility model)"
    data_source = place_repository.get_data_source_name()
    total_places = len(place_repository.get_all_places())

    return HealthResponse(
        status="HEALTHY",
        service=settings.service_name,
        version=settings.api_version,
        routes_api_status=routes_status,
        data_source=data_source,
        total_places_loaded=total_places
    )

@app.post("/plan", response_model=PlanResponse, tags=["Planning"])
async def create_itinerary_plan(
    plan_request: PlanRequest,
    current_user: Optional[dict] = Depends(optional_firebase_auth)
):
    """
    Generates a personalized, time-aware, budget-aware single-day itinerary for Mumbai.
    - Validates start location, duration, budget, and desired interests.
    - Filters candidate places against opening hours, facilities, and hard constraints.
    - Sequences stops to minimize travel time using route estimates and 2-opt routing.
    - Provides detailed warnings for unverified prices, estimated routes, or unknown opening hours.
    """
    try:
        user_id = current_user.get("uid") if current_user else "anonymous"
        logger.info(f"Generating plan for user '{user_id}' on {plan_request.date} starting at {plan_request.start_time}")
        response = itinerary_planner.plan_itinerary(plan_request)
        return response
    except Exception as e:
        logger.exception(f"Unhandled error during itinerary generation: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"An error occurred while generating the itinerary: {str(e)}"
        )

@app.post("/evaluate/ranking", tags=["Machine Learning"])
async def evaluate_ranking(
    plan_request: PlanRequest
):
    """
    Compares the transparent rule-based baseline ranking against the
    content-based TF-IDF and cosine similarity model on the same candidate set.
    """
    all_places = place_repository.get_all_places()
    start_lat = plan_request.start_location.latitude if plan_request.start_location else 18.9328
    start_lon = plan_request.start_location.longitude if plan_request.start_location else 72.8329

    comparison = content_recommender.compare_with_baseline(
        all_places, plan_request, start_lat, start_lon, plan_request.budget_inr
    )
    return comparison
