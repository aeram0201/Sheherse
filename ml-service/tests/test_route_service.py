from app.core.route_service import RouteService, haversine_distance_meters

def test_haversine_formula():
    # CST to Marine Drive (~2.1 km straight line)
    # CST: 18.9406, 72.8354; Marine Drive: 18.9438, 72.8231
    dist_m = haversine_distance_meters(18.9406, 72.8354, 18.9438, 72.8231)
    assert 1200 <= dist_m <= 1600

def test_fallback_route_calculation_never_zero():
    svc = RouteService(api_key="")  # Unconfigured key
    # CST to Gateway / Colaba
    route = svc.get_travel_estimate(18.9406, 72.8354, 18.9137, 72.8225, mode="WALKING")
    assert route.is_estimated is True
    assert route.distance_meters > 2500
    assert route.duration_minutes > 15
    assert "HAVERSINE_MUMBAI_MODEL" in route.estimation_basis
    assert route.warning is not None

def test_transport_mode_speed_differences():
    svc = RouteService(api_key="")
    lat1, lon1 = 18.9406, 72.8354
    lat2, lon2 = 19.0163, 72.8306  # CST to Prabhadevi (~9 km)

    walk = svc.get_travel_estimate(lat1, lon1, lat2, lon2, mode="WALKING")
    drive = svc.get_travel_estimate(lat1, lon1, lat2, lon2, mode="DRIVING")

    # Walking takes much longer than driving for 9 km
    assert walk.duration_minutes > drive.duration_minutes
    assert walk.distance_meters == drive.distance_meters

def test_same_location_negligible_travel():
    svc = RouteService(api_key="")
    route = svc.get_travel_estimate(18.9406, 72.8354, 18.94061, 72.83541, mode="WALKING")
    assert route.distance_meters < 50
    assert route.duration_minutes == 1
