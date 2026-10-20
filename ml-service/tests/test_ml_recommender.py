from app.ml.content_recommender import content_recommender
from app.data.place_repository import place_repository

def test_tfidf_ranking_execution(sample_valid_request):
    places = place_repository.get_all_places()
    ranked = content_recommender.rank_places_tfidf(
        places, sample_valid_request, 18.9328, 72.8329, 500
    )
    assert len(ranked) == len(places)
    assert all(hasattr(item, "score") for item in ranked)
    # Highest scoring place should be first
    assert ranked[0].score >= ranked[-1].score

def test_baseline_vs_ml_comparison(sample_valid_request):
    places = place_repository.get_all_places()
    comparison = content_recommender.compare_with_baseline(
        places, sample_valid_request, 18.9328, 72.8329, 500
    )
    assert "top_baseline" in comparison
    assert "top_ml" in comparison
    assert "comparisons" in comparison
    assert len(comparison["comparisons"]) > 0

def test_api_ranking_comparison_endpoint(client, sample_valid_request):
    payload = sample_valid_request.model_dump()
    response = client.post("/evaluate/ranking", json=payload)
    assert response.status_code == 200
    data = response.json()
    assert "top_baseline" in data
    assert "top_ml" in data
    assert len(data["comparisons"]) > 0
