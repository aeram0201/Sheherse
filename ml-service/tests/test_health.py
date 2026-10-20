def test_health_endpoint(client):
    response = client.get("/health")
    assert response.status_code == 200
    data = response.json()
    assert data["status"] == "HEALTHY"
    assert data["version"] == "v1"
    assert data["total_places_loaded"] >= 30
    assert "data_source" in data
    assert "routes_api_status" in data
