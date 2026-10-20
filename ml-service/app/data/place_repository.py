import json
import logging
from pathlib import Path
from typing import List, Optional, Dict
from app.models.place import Place
from app.data.firestore_client import get_firestore_db

logger = logging.getLogger("sheherse.place_repo")

class PlaceRepository:
    def __init__(self, seed_file_path: Optional[str] = None):
        self._places_cache: Dict[str, Place] = {}
        if seed_file_path:
            self._seed_file_path = Path(seed_file_path)
        else:
            self._seed_file_path = Path(__file__).resolve().parent / "seed_places.json"
        self._load_seed_places()

    def _load_seed_places(self):
        try:
            if self._seed_file_path.exists():
                with open(self._seed_file_path, "r", encoding="utf-8") as f:
                    data = json.load(f)
                    for item in data:
                        p = Place(**item)
                        self._places_cache[p.id] = p
                logger.info(f"Loaded {len(self._places_cache)} places from local seed dataset.")
            else:
                logger.error(f"Seed file not found at {self._seed_file_path}")
        except Exception as e:
            logger.error(f"Error loading seed places: {e}")

    def get_data_source_name(self) -> str:
        db = get_firestore_db()
        if db is not None:
            return "Firestore (Live Cloud Database)"
        return "Local Curated Mumbai Seed Dataset"

    def get_all_places(self) -> List[Place]:
        db = get_firestore_db()
        if db is not None:
            try:
                places_ref = db.collection("places").stream()
                cloud_places = []
                for doc in places_ref:
                    doc_data = doc.to_dict()
                    doc_data["id"] = doc.id
                    cloud_places.append(Place(**doc_data))
                if cloud_places:
                    return cloud_places
            except Exception as e:
                logger.warning(f"Error fetching places from Firestore: {e}. Using cached seed data.")

        return list(self._places_cache.values())

    def get_place_by_id(self, place_id: str) -> Optional[Place]:
        if not place_id:
            return None

        # Check local cache first
        if place_id in self._places_cache:
            return self._places_cache[place_id]

        db = get_firestore_db()
        if db is not None:
            try:
                doc = db.collection("places").document(place_id).get()
                if doc.exists:
                    doc_data = doc.to_dict()
                    doc_data["id"] = doc.id
                    p = Place(**doc_data)
                    self._places_cache[p.id] = p
                    return p
            except Exception as e:
                logger.warning(f"Error fetching place {place_id} from Firestore: {e}")

        return None

    def get_places_by_ids(self, place_ids: List[str]) -> List[Place]:
        result = []
        for pid in place_ids:
            p = self.get_place_by_id(pid)
            if p:
                result.append(p)
        return result

# Global repository instance
place_repository = PlaceRepository()
