import math
import re
from typing import List, Dict, Tuple, Set
from app.models.place import Place
from app.models.request import PlanRequest
from app.core.route_service import haversine_distance_meters

class ScoredPlace:
    def __init__(self, place: Place, score: float, rationale: str):
        self.place = place
        self.score = score
        self.rationale = rationale

def score_places_baseline(
    places: List[Place],
    request: PlanRequest,
    current_lat: float,
    current_lon: float,
    remaining_budget: int,
    finish_lat: float = 0.0,
    finish_lon: float = 0.0,
    is_later_in_day: bool = False
) -> List[ScoredPlace]:
    """
    Personalised scoring function combining:
    1. User preferred places (+100)
    2. Category relevance (+35)
    3. Tag overlap (+10 per tag, max 30)
    4. Specific experience notes text matching (+20)
    5. Food preference matching (Veg/Non-Veg) (+15 / -25)
    6. Traveler type adaptation (Family/Children/Solo) (+15)
    7. Experience tier (Budget/Comfort) (+15)
    8. Rating & Verification quality (+10..50)
    9. Hidden gem bonus (+15)
    10. Budget conservation bonus (+10..20)
    11. Spatial proximity to current location
    12. Convergence to finish destination towards end of day
    """
    scored = []
    preferred_set = set(request.preferred_place_ids)
    interests_lower = set(i.lower() for i in request.interests)

    # Tokenize specific notes if provided
    note_tokens: Set[str] = set()
    if request.specific_notes:
        note_tokens = set(re.findall(r'\b[a-z]{3,}\b', request.specific_notes.lower()))

    # Food preference normalization
    food_pref = (request.food_preference or "NO_PREFERENCE").upper()
    traveler_type = (request.traveler_type or "SOLO").upper()
    exp_tier = (request.experience_tier or "BALANCED").upper()

    for place in places:
        score = 0.0
        reasons = []

        # 1. Preferred place boost
        if place.id in preferred_set:
            score += 100.0
            reasons.append("Must-visit selection")

        # 2. Category match
        if place.category.lower() in interests_lower:
            score += 35.0
            reasons.append(f"Matches category '{place.category}'")

        # 3. Tag overlap
        tag_matches = []
        for tag in place.tags:
            t_low = tag.lower()
            if t_low in interests_lower or any(i in t_low for i in interests_lower):
                tag_matches.append(tag)
        if tag_matches:
            tag_score = min(len(tag_matches) * 10.0, 30.0)
            score += tag_score
            reasons.append(f"Tag match: {', '.join(tag_matches[:2])}")

        # 4. Specific notes match
        if note_tokens:
            place_text = f"{place.name} {place.description} {' '.join(place.tags)} {place.area} {place.insiderTip or ''}".lower()
            matching_tokens = [tok for tok in note_tokens if tok in place_text]
            if matching_tokens:
                score += min(len(matching_tokens) * 12.0, 30.0)
                reasons.append(f"Matches wish: '{', '.join(matching_tokens[:2])}'")

        # 5. Food preference matching
        if place.category == "Food":
            desc_lower = (place.description + " " + " ".join(place.tags) + " " + (place.cuisineType or "")).lower()
            if food_pref in ("VEGETARIAN", "VEGAN"):
                if any(m in desc_lower for m in ["kheema", "mutton", "fish", "meat", "non-veg"]):
                    score -= 25.0
                elif any(v in desc_lower for v in ["veg", "pure veg", "samosa", "chole", "pav bhaji", "puri"]):
                    score += 15.0
                    reasons.append("Vegetarian friendly")
            elif food_pref == "NON_VEGETARIAN":
                if any(m in desc_lower for m in ["kheema", "mutton", "fish", "sali boti", "berry pulao"]):
                    score += 20.0
                    reasons.append("Signature non-veg delicacies")

        # 6. Traveler type adaptations
        if traveler_type in ("FAMILY", "CHILDREN"):
            if place.category in ("Nature", "Historical") or any(t in place.tags for t in ["Park", "Garden", "Caves", "Marine Drive", "Art Deco"]):
                score += 15.0
                reasons.append("Great for family outing")
            if "Restroom" in place.facilities and "Seating" in place.facilities:
                score += 10.0
        elif traveler_type == "SOLO":
            if place.category == "Study" or (place.quietnessLevel and place.quietnessLevel.lower() in ("silent", "moderate")):
                score += 12.0
                reasons.append("Ideal for solo contemplation")

        # 7. Experience tier adaptations
        if exp_tier == "BUDGET":
            if place.estimatedCost == 0:
                score += 20.0
                reasons.append("Free entry")
            elif place.estimatedCost <= 60:
                score += 12.0
                reasons.append("Budget friendly")
        elif exp_tier == "COMFORT":
            if "AC" in place.facilities or "Seating" in place.facilities:
                score += 15.0
                reasons.append("Comfortable amenities")

        # 8. Rating and verification
        quality_score = place.rating * 8.0
        if place.verificationStatus == "VERIFIED":
            quality_score += 5.0
        score += quality_score

        # 9. Hidden gem bonus
        if place.isHiddenGem and any(term in interests_lower for term in ("hidden", "gem", "offbeat", "heritage", "walk")):
            score += 15.0
            reasons.append("Curated hidden gem")

        # 10. Budget conservation bonus
        if remaining_budget > 0:
            if place.estimatedCost == 0:
                score += 10.0
            elif place.estimatedCost <= (remaining_budget * 0.25):
                score += 8.0

        # 11. Spatial proximity penalty to current location
        if place.has_valid_coordinates():
            dist_m = haversine_distance_meters(current_lat, current_lon, place.latitude, place.longitude)
            dist_km = dist_m / 1000.0
            dist_penalty = min(dist_km * 1.5, 30.0)
            score -= dist_penalty
            if dist_km <= 2.5:
                reasons.append(f"Nearby ({dist_km:.1f} km)")

            # 12. Proximity to finish location towards end of day
            if is_later_in_day and finish_lat != 0.0 and finish_lon != 0.0:
                dist_to_finish = haversine_distance_meters(finish_lat, finish_lon, place.latitude, place.longitude) / 1000.0
                if dist_to_finish <= 4.0:
                    score += 18.0
                    reasons.append(f"Close to destination ({dist_to_finish:.1f} km)")
                else:
                    score -= min(dist_to_finish * 1.0, 20.0)

        rationale_str = "; ".join(reasons) if reasons else f"Top rated {place.category} spot in {place.area}"
        scored.append(ScoredPlace(place=place, score=round(score, 2), rationale=rationale_str))

    # Sort descending by score
    scored.sort(key=lambda x: x.score, reverse=True)
    return scored
