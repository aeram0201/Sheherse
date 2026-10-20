import re
from typing import List, Dict, Any, Tuple
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity
from app.models.place import Place
from app.models.request import PlanRequest
from app.core.ranking import ScoredPlace, score_places_baseline
from app.core.route_service import haversine_distance_meters

class ContentBasedRecommender:
    """
    Content-based recommendation experiment using TF-IDF and Cosine Similarity
    over place descriptions, tags, insider tips, and categories.

    Note: This is a content-based text similarity model, not a collaborative
    filtering model. It does NOT claim to learn from user interaction logs.
    """
    def __init__(self):
        self.vectorizer = TfidfVectorizer(
            stop_words="english",
            ngram_range=(1, 2),
            lowercase=True,
            max_features=500
        )

    def _build_place_document(self, place: Place) -> str:
        parts = [
            place.name,
            place.category,
            place.area,
            place.description,
            " ".join(place.tags),
            place.cuisineType or "",
            place.quietnessLevel or "",
            place.insiderTip or ""
        ]
        return " ".join(parts)

    def _build_user_query(self, request: PlanRequest) -> str:
        parts = list(request.interests)
        if request.specific_notes:
            parts.append(request.specific_notes)
        if request.food_preference and request.food_preference != "NO_PREFERENCE":
            parts.append(request.food_preference)
        if request.traveler_type:
            parts.append(request.traveler_type)
        if request.accessibility_needs:
            parts.extend(request.accessibility_needs)
        if request.preferred_place_ids:
            parts.append("heritage discovery iconic")
        return " ".join(parts)

    def rank_places_tfidf(
        self,
        places: List[Place],
        request: PlanRequest,
        current_lat: float,
        current_lon: float,
        remaining_budget: int
    ) -> List[ScoredPlace]:
        if not places:
            return []

        # Build corpus
        corpus = [self._build_place_document(p) for p in places]
        user_query = self._build_user_query(request)

        # If user query is empty, fallback to baseline
        if not user_query.strip():
            return score_places_baseline(places, request, current_lat, current_lon, remaining_budget)

        # Fit TF-IDF on corpus + user query
        tfidf_matrix = self.vectorizer.fit_transform(corpus + [user_query])
        place_vectors = tfidf_matrix[:-1]
        user_vector = tfidf_matrix[-1:]

        similarities = cosine_similarity(user_vector, place_vectors).flatten()

        preferred_set = set(request.preferred_place_ids)
        scored = []

        for i, place in enumerate(places):
            text_sim = float(similarities[i])  # 0.0 to 1.0
            # Scale text similarity to 0..50 points
            score = text_sim * 50.0
            reasons = [f"Content text match: {text_sim:.2f}"]

            if place.id in preferred_set:
                score += 100.0
                reasons.append("Explicitly preferred by user")

            # Add quality factor
            quality = place.rating * 6.0
            if place.verificationStatus == "VERIFIED":
                quality += 5.0
            score += quality

            # Spatial decay
            if place.has_valid_coordinates():
                dist_m = haversine_distance_meters(current_lat, current_lon, place.latitude, place.longitude)
                dist_km = dist_m / 1000.0
                dist_penalty = min(dist_km * 1.5, 30.0)
                score -= dist_penalty
                if dist_km <= 2.5:
                    reasons.append(f"Nearby ({dist_km:.1f} km)")

            rationale_str = "; ".join(reasons)
            scored.append(ScoredPlace(place=place, score=round(score, 2), rationale=rationale_str))

        scored.sort(key=lambda x: x.score, reverse=True)
        return scored

    def compare_with_baseline(
        self,
        places: List[Place],
        request: PlanRequest,
        current_lat: float,
        current_lon: float,
        remaining_budget: int = 500
    ) -> Dict[str, Any]:
        """
        Runs both baseline rule-based ranking and TF-IDF content similarity ranking,
        returning top candidates and rank correlation for evaluation.
        """
        baseline_results = score_places_baseline(places, request, current_lat, current_lon, remaining_budget)
        ml_results = self.rank_places_tfidf(places, request, current_lat, current_lon, remaining_budget)

        baseline_rank_map = {item.place.id: idx for idx, item in enumerate(baseline_results)}
        ml_rank_map = {item.place.id: idx for idx, item in enumerate(ml_results)}

        comparisons = []
        for pid in baseline_rank_map:
            b_rank = baseline_rank_map[pid]
            m_rank = ml_rank_map[pid]
            p_obj = next(item.place for item in baseline_results if item.place.id == pid)
            comparisons.append({
                "place_id": pid,
                "name": p_obj.name,
                "baseline_rank": b_rank + 1,
                "baseline_score": next(item.score for item in baseline_results if item.place.id == pid),
                "ml_rank": m_rank + 1,
                "ml_score": next(item.score for item in ml_results if item.place.id == pid),
                "rank_delta": b_rank - m_rank  # positive means higher rank in ML
            })

        return {
            "total_evaluated": len(comparisons),
            "top_baseline": [item.place.name for item in baseline_results[:5]],
            "top_ml": [item.place.name for item in ml_results[:5]],
            "comparisons": comparisons[:10]
        }

content_recommender = ContentBasedRecommender()
