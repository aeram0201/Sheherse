from app.core.opening_hours import check_opening_hours

def test_open_24_hours():
    # Marine Drive: Open 24 hours
    res = check_opening_hours("Open 24 hours", "2026-10-21", 120, 180)  # 2:00 AM to 3:00 AM
    assert res.is_known is True
    assert res.is_open is True
    assert res.status_code == "CONFIRMED_OPEN"

def test_closed_sundays_detected():
    # 2026-10-25 is a Sunday
    hours = "11:30 AM – 4:00 PM (Closed Sundays)"
    res_sun = check_opening_hours(hours, "2026-10-25", 720, 780)  # 12:00 PM to 1:00 PM on Sunday
    assert res_sun.is_known is True
    assert res_sun.is_open is False
    assert res_sun.status_code == "CLOSED_ON_DAY"
    assert "Sundays" in res_sun.warning

    # 2026-10-21 is a Wednesday
    res_wed = check_opening_hours(hours, "2026-10-21", 720, 780)  # 12:00 PM to 1:00 PM on Wednesday
    assert res_wed.is_known is True
    assert res_wed.is_open is True
    assert res_wed.status_code == "CONFIRMED_OPEN"

def test_closed_mondays_detected():
    # 2026-10-26 is a Monday (Kanheri Caves closed Mondays)
    hours = "7:30 AM – 5:00 PM (Closed Mondays)"
    res_mon = check_opening_hours(hours, "2026-10-26", 600, 720)
    assert res_mon.is_open is False
    assert res_mon.status_code == "CLOSED_ON_DAY"

def test_outside_operating_hours():
    # Kitab Khana: 10:30 AM – 7:30 PM (630m to 1170m)
    hours = "10:30 AM – 7:30 PM"
    # Arrival at 08:00 AM (480m)
    res_early = check_opening_hours(hours, "2026-10-21", 480, 540)
    assert res_early.is_open is False
    assert res_early.status_code == "OUTSIDE_HOURS"

    # Arrival at 2:00 PM (840m)
    res_normal = check_opening_hours(hours, "2026-10-21", 840, 900)
    assert res_normal.is_open is True
    assert res_normal.status_code == "CONFIRMED_OPEN"

def test_unknown_opening_hours_never_assumed_open():
    # Empty or None opening hours must NEVER be assumed open
    res_empty = check_opening_hours("", "2026-10-21", 600, 660)
    assert res_empty.is_known is False
    assert res_empty.is_open is False
    assert res_empty.status_code == "HOURS_UNKNOWN"
    assert res_empty.warning is not None

    res_none = check_opening_hours(None, "2026-10-21", 600, 660)
    assert res_none.is_known is False
    assert res_none.is_open is False
    assert res_none.status_code == "HOURS_UNKNOWN"
