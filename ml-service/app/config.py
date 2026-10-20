import os
from pathlib import Path
from pydantic import BaseModel
from dotenv import load_dotenv

# Load .env file from ml-service root if present
env_path = Path(__file__).resolve().parent.parent / ".env"
load_dotenv(dotenv_path=env_path)

class Settings(BaseModel):
    service_name: str = os.getenv("SERVICE_NAME", "SheherSe-Itinerary-Engine")
    api_version: str = os.getenv("API_VERSION", "v1")
    debug: bool = os.getenv("DEBUG", "False").lower() in ("true", "1", "yes")
    host: str = os.getenv("HOST", "0.0.0.0")
    port: int = int(os.getenv("PORT", "8000"))

    # Google Maps Routes API Key
    google_maps_api_key: str = os.getenv("GOOGLE_MAPS_API_KEY", "")

    # Firebase Admin SDK Configuration
    firebase_credentials_path: str = os.getenv("FIREBASE_CREDENTIALS_PATH", "")
    require_firebase_auth: bool = os.getenv("REQUIRE_FIREBASE_AUTH", "False").lower() in ("true", "1", "yes")

    # Engine Speed & Physics Models for Mumbai
    speed_walk_kmh: float = float(os.getenv("SPEED_WALK_KMH", "4.5"))
    speed_transit_kmh: float = float(os.getenv("SPEED_TRANSIT_KMH", "18.0"))
    speed_drive_kmh: float = float(os.getenv("SPEED_DRIVE_KMH", "22.0"))
    mumbai_road_detour_factor: float = float(os.getenv("MUMBAI_ROAD_DETOUR_FACTOR", "1.35"))
    default_buffer_minutes: int = int(os.getenv("DEFAULT_BUFFER_MINUTES", "15"))

    def get_masked_api_key(self) -> str:
        if not self.google_maps_api_key:
            return "NOT_CONFIGURED"
        if len(self.google_maps_api_key) <= 8:
            return "***"
        return f"{self.google_maps_api_key[:4]}...{self.google_maps_api_key[-4:]}"

settings = Settings()
