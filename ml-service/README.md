# SheherSe — Day Itinerary Planning Engine (ML Service)

This service provides deterministic, personalized, geographically sensible, time-aware, and budget-aware single-day itineraries for **SheherSe**, a Mumbai discovery app.

---

## 1. System Architecture

```
ml-service/
├── app/
│   ├── main.py                  # FastAPI application & endpoints (/health, /plan, /evaluate/ranking)
│   ├── config.py                # Environment configuration & secret masking
│   ├── models/
│   │   ├── place.py             # Place schema matching Android Place.java & Firestore
│   │   ├── request.py           # PlanRequest & validation rules
│   │   └── response.py          # PlanResponse, StopResponse, TravelLeg, Summary & Warnings
│   ├── data/
│   │   ├── seed_places.json     # 30 verified Mumbai seed places from PlaceRepository
│   │   ├── place_repository.py  # Repository with dual Firestore / Seed dataset support
│   │   └── firestore_client.py  # Firebase Admin SDK & token validation
│   ├── core/
│   │   ├── opening_hours.py     # Opening hours parser & day-of-week closure check
│   │   ├── route_service.py     # Google Maps Routes API client + Mumbai mobility fallback
│   │   ├── filter.py            # Hard-constraint filtering & diagnostics
│   │   ├── ranking.py           # Multi-criteria rule-based baseline scoring
│   │   ├── scheduler.py         # Time simulation, buffers & pacing logic
│   │   └── planner.py           # Core planner, stop sequencing & schedule verification
│   └── ml/
│       └── content_recommender.py # TF-IDF + Cosine similarity experiment
├── tests/                       # 32 automated pytest test cases
├── run_server.py                # Entrypoint script to run uvicorn server
├── run_tests.py                 # Entrypoint script to execute test suite
├── run_server.bat               # Windows double-click server runner
├── run_tests.bat                # Windows double-click test runner
├── requirements.txt             # Documented Python dependencies
├── .env.example                 # Environment template with placeholders only
└── .gitignore                   # Excludes secrets, .env and virtual environments
```

---

## 2. Setup and Run Instructions (Windows)

### Prerequisites
- Python 3.10+ (or Python 3.11)
- Windows PowerShell or Command Prompt

### Step 1: Create Virtual Environment
```powershell
cd ml-service
python -m venv venv
```

### Step 2: Install Dependencies
```powershell
.\venv\Scripts\pip install -r requirements.txt
```

### Step 3: Configure Environment Variables
Copy `.env.example` to `.env`:
```powershell
Copy-Item .env.example .env
```
*(Optional)* Add your `GOOGLE_MAPS_API_KEY` or `FIREBASE_CREDENTIALS_PATH`. If left blank, the service operates seamlessly using the built-in deterministic Mumbai mobility model and local curated seed places.

### Step 4: Run Automated Tests
```powershell
python run_tests.py
# or double-click run_tests.bat
```

### Step 5: Start the Server
```powershell
python run_server.py
# or double-click run_server.bat
```
The server will start at `http://localhost:8000`. Interactive OpenAPI documentation will be accessible at:
- **Swagger UI**: `http://localhost:8000/docs`
- **ReDoc**: `http://localhost:8000/redoc`

---

## 3. Environment Variables Reference

| Variable | Description | Default |
|---|---|---|
| `HOST` | Server host binding | `0.0.0.0` |
| `PORT` | Server port binding | `8000` |
| `DEBUG` | Enable debug logging & reload | `False` |
| `GOOGLE_MAPS_API_KEY` | Server-restricted Google Maps API key with Routes API enabled | `""` (uses deterministic fallback) |
| `FIREBASE_CREDENTIALS_PATH` | Path to service account JSON (never commit secrets) | `""` (uses seed dataset) |
| `REQUIRE_FIREBASE_AUTH` | Enforce Bearer token verification on requests | `False` |
| `SPEED_WALK_KMH` | Walking speed model for Mumbai | `4.5` km/h |
| `SPEED_TRANSIT_KMH` | Public transit speed model (BEST bus / suburban train) | `18.0` km/h |
| `SPEED_DRIVE_KMH` | Driving / Taxi speed model | `22.0` km/h |
| `MUMBAI_ROAD_DETOUR_FACTOR` | Peninsula road network detour factor over Haversine | `1.35` |

---

## 4. API Contract for Android Developers

### Endpoint 1: Health Check
- **Method**: `GET /health`
- **Description**: Verifies service status, active data source, and Google Maps integration status.
- **Response**:
```json
{
  "status": "HEALTHY",
  "service": "SheherSe-Itinerary-Engine",
  "version": "v1",
  "routes_api_status": "FALLBACK (Mumbai mobility model)",
  "data_source": "Local Curated Mumbai Seed Dataset",
  "total_places_loaded": 30
}
```

---

### Endpoint 2: Generate Day Plan
- **Method**: `POST /plan`
- **Headers**:
  - `Content-Type: application/json`
  - *(Optional)* `Authorization: Bearer <firebase_id_token>`

#### Request Body Schema (`PlanRequest`)
```json
{
  "start_location": {
    "latitude": 18.9328,
    "longitude": 72.8329
  },
  "start_place_id": null,
  "date": "2026-10-21",
  "start_time": "09:30",
  "available_duration_minutes": 360,
  "budget_inr": 500,
  "interests": ["Historical", "Food", "Study"],
  "transport_mode": "WALKING",
  "preferred_place_ids": ["place_kyani"],
  "avoid_place_ids": [],
  "accessibility_needs": ["Seating"],
  "pace_preference": "BALANCED",
  "use_ml_ranking": false
}
```

#### Field Explanations:
- `start_location` *(object, optional)*: User's starting GPS coordinates (`latitude`, `longitude`). Provide either `start_location` or `start_place_id`.
- `start_place_id` *(string, optional)*: A known place ID to start the day from (e.g. `"place_kitab_khana"`).
- `date` *(string, required)*: `YYYY-MM-DD`. Used to calculate exact day of week (e.g. filtering out places closed on Sundays or 2nd/4th Saturdays).
- `start_time` *(string, required)*: `HH:MM` in 24-hour format (e.g. `"09:30"`).
- `available_duration_minutes` *(integer, required)*: Available time window (30 to 1440 minutes).
- `budget_inr` *(integer, required)*: Target maximum budget in INR (e.g. `500`).
- `interests` *(array of strings)*: Desired categories (e.g. `["Historical", "Food", "Study", "Sunset", "Cultural", "Nature", "Market"]`).
- `transport_mode` *(string)*: `"WALKING"`, `"TRANSIT"`, `"DRIVING"`, or `"TWO_WHEELER"`.
- `preferred_place_ids` *(array of strings, optional)*: Place IDs explicitly prioritized for inclusion.
- `avoid_place_ids` *(array of strings, optional)*: Place IDs excluded from the itinerary.
- `accessibility_needs` *(array of strings, optional)*: Required facilities (e.g. `["Seating", "Restroom", "AC", "Quiet", "WiFi", "Charging"]`).
- `pace_preference` *(string, optional)*: `"RELAXED"`, `"BALANCED"`, or `"PACKED"`.
- `use_ml_ranking` *(boolean, optional)*: If `true`, applies TF-IDF text similarity scoring instead of the baseline rule-based ranker.

#### Response Body Schema (`PlanResponse`)
```json
{
  "itinerary_id": "itin_90f23b1a8d",
  "title": "Historical Day Trail in Fort, South Mumbai",
  "summary": {
    "total_stops": 3,
    "total_duration_minutes": 272,
    "total_travel_minutes": 32,
    "total_visit_minutes": 210,
    "total_cost_inr": 280,
    "cost_is_fully_known": false,
    "budget_inr": 500,
    "remaining_budget_inr": 220,
    "is_feasible": true
  },
  "stops": [
    {
      "stop_id": "stop_1_place_kyani",
      "place_id": "place_kyani",
      "order_index": 0,
      "place": {
        "id": "place_kyani",
        "name": "Kyani & Co. Bakery & Irani Chai",
        "description": "Operating continuously since 1904...",
        "category": "Food",
        "area": "Marine Lines, South Mumbai",
        "address": "Jermahal Estate, 657, JSS Rd, Marine Lines, Mumbai 400002",
        "estimatedCost": 80,
        "priceEstimated": true,
        "durationMinutes": 45,
        "latitude": 18.9431,
        "longitude": 72.8272,
        "tags": ["Irani Cafe", "Bun Maska", "Budget Food", "Heritage"],
        "facilities": ["Seating", "Restroom"],
        "verificationStatus": "VERIFIED",
        "isHiddenGem": false,
        "isStudyWork": false,
        "isAffordableFood": true,
        "cuisineType": "Parsi/Irani",
        "quietnessLevel": "Lively",
        "rating": 4.7,
        "openingHours": "7:00 AM – 8:30 PM",
        "insiderTip": "Dip fresh brun pav into hot Irani chai...",
        "ownerId": "owner_kyani"
      },
      "arrival_time": "09:51",
      "departure_time": "10:36",
      "duration_minutes": 45,
      "cost_inr": 80,
      "price_estimated": true,
      "cost_note": "₹80 (Estimated cost)",
      "opening_status": "CONFIRMED_OPEN",
      "recommendation_reason": "Explicitly preferred by user; Matches category 'Food'",
      "custom_note": "Dip fresh brun pav into hot Irani chai..."
    }
  ],
  "travel_legs": [
    {
      "from_title": "Starting Point",
      "to_title": "Kyani & Co. Bakery & Irani Chai",
      "from_place_id": null,
      "to_place_id": "place_kyani",
      "mode": "WALKING",
      "distance_meters": 1563,
      "duration_minutes": 21,
      "is_estimated": true,
      "estimation_basis": "HAVERSINE_MUMBAI_MODEL (detour=1.35x, speed=4.5km/h, buffer=0m)",
      "route_warning": "Route estimated via Mumbai urban mobility model (Google Maps API key not configured)."
    }
  ],
  "warnings": [
    {
      "code": "UNKNOWN_EXACT_PRICE",
      "message": "Exact cost for Kyani & Co. Bakery & Irani Chai is not published; estimated at ₹80.",
      "place_id": "place_kyani",
      "details": null
    },
    {
      "code": "ROUTE_ESTIMATED",
      "message": "Leg from 'Starting Point' to 'Kyani & Co. Bakery & Irani Chai': Route estimated via Mumbai urban mobility model.",
      "place_id": "place_kyani",
      "details": null
    }
  ],
  "planning_rationale": "Crafted a 3-stop itinerary tailored to Historical, Food, Study. Stops are clustered in Fort, South Mumbai to minimize travel overhead (32 mins travel vs 210 mins exploration). Scheduled from 09:30 with balanced pacing.",
  "infeasibility_reasons": [],
  "alternative_suggestions": []
}
```

---

### Endpoint 3: Recommendation Evaluation & Comparison
- **Method**: `POST /evaluate/ranking`
- **Description**: Evaluates rule-based baseline ranking against scikit-learn TF-IDF text cosine similarity.
- **Response**:
```json
{
  "total_evaluated": 29,
  "top_baseline": [
    "Kyani & Co. Bakery & Irani Chai",
    "David Sassoon Library & Reading Room",
    "Kitab Khana & Somethings Gourmet Cafe"
  ],
  "top_ml": [
    "Kyani & Co. Bakery & Irani Chai",
    "Kitab Khana & Somethings Gourmet Cafe",
    "David Sassoon Library & Reading Room"
  ],
  "comparisons": [
    {
      "place_id": "place_kyani",
      "name": "Kyani & Co. Bakery & Irani Chai",
      "baseline_rank": 1,
      "baseline_score": 158.4,
      "ml_rank": 1,
      "ml_score": 158.8,
      "rank_delta": 0
    }
  ]
}
```

---

## 5. Automated Tests Summary

The test suite contains **32 tests** in `tests/`:
- `test_validation.py` (7 tests): Date format, time format, duration limits, negative budget, conflicting preferred/avoid places, missing coordinates, API 422 handler.
- `test_opening_hours.py` (5 tests): 24/7 hours, closed Sundays, closed Mondays, outside operating hours, unconfirmed/empty hours.
- `test_route_service.py` (4 tests): Haversine distance, non-zero fallback calculation, speed differences across modes, adjacent location handling.
- `test_planner.py` (4 tests): End-to-end plan generation, no duplicate place IDs, avoid places respected, API POST /plan integration.
- `test_budget_constraints.py` (3 tests): Zero budget only schedules free places, strict budget bounds, total cost consistency.
- `test_missing_data_warnings.py` (4 tests): Estimated price warnings, estimated route warnings, invalid coordinate filtering, empty candidate set explanations.
- `test_schedule_consistency.py` (1 test): Strict timeline arithmetic (`departure == arrival + duration`, `arrival >= prev_departure + travel`).
- `test_ml_recommender.py` (3 tests): TF-IDF ranking execution, baseline vs ML comparison, API `/evaluate/ranking` integration.
- `test_health.py` (1 test): Health status, version, loaded places count.

**Result**: 32 passed in 0.46s.

---

## 6. Known Limitations & Recommendations

1. **Live Google Routes**: When `GOOGLE_MAPS_API_KEY` is not set, travel durations are estimated using a calibrated Mumbai peninsula mobility model (1.35x detour factor). For live real-time traffic conditions, configure a Google Maps API key with Routes API enabled.
2. **Opening Hours Formats**: Curated human-readable opening hours are parsed with regular expressions. Places with complex holiday schedules or unverified hours are labeled with `UNCONFIRMED_OPENING_HOURS` so users are prompted to verify before departing.
3. **Machine Learning Model**: The content-based TF-IDF recommender measures semantic keyword relevance between place descriptions and user interests. It is not collaborative filtering and does not claim to learn from user behaviour without genuine interaction logs.
