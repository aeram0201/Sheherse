import re
from datetime import datetime
from typing import Optional, Tuple
from pydantic import BaseModel

class OpeningHoursResult(BaseModel):
    is_known: bool
    is_open: bool
    status_code: str  # CONFIRMED_OPEN, HOURS_UNKNOWN, CLOSED_ON_DAY, OUTSIDE_HOURS
    warning: Optional[str] = None
    open_time_str: Optional[str] = None
    close_time_str: Optional[str] = None
    open_minutes: Optional[int] = None
    close_minutes: Optional[int] = None

def _time_str_to_minutes(time_str: str) -> Optional[int]:
    """Converts '5:00 AM', '12:30 PM', '7 PM', '10:00AM' to minutes from midnight."""
    cleaned = time_str.strip().upper()
    match = re.match(r"^(\d{1,2})(?::(\d{2}))?\s*(AM|PM)$", cleaned)
    if not match:
        return None
    hour = int(match.group(1))
    minute = int(match.group(2) or 0)
    meridiem = match.group(3)

    if meridiem == "AM":
        if hour == 12:
            hour = 0
    elif meridiem == "PM":
        if hour != 12:
            hour += 12

    return hour * 60 + minute

def check_opening_hours(
    opening_hours_str: Optional[str],
    date_str: str,
    arrival_minutes: int,
    departure_minutes: int
) -> OpeningHoursResult:
    """
    Evaluates whether a place is confirmed open during [arrival_minutes, departure_minutes]
    on the specified date.

    Never assumes open when hours are unknown or missing.
    """
    if not opening_hours_str or not opening_hours_str.strip():
        return OpeningHoursResult(
            is_known=False,
            is_open=False,
            status_code="HOURS_UNKNOWN",
            warning="Opening hours unavailable. Cannot confirm if place is open."
        )

    raw = opening_hours_str.strip()

    # 1. Determine day of week
    try:
        dt = datetime.strptime(date_str, "%Y-%m-%d")
        weekday = dt.weekday()  # Monday is 0, Sunday is 6
        day_of_month = dt.day
    except ValueError:
        return OpeningHoursResult(
            is_known=False,
            is_open=False,
            status_code="HOURS_UNKNOWN",
            warning=f"Invalid date '{date_str}' for opening hours check."
        )

    lower_raw = raw.lower()

    # 2. Check Day-of-Week Closures
    if "closed sundays" in lower_raw or "closed sunday" in lower_raw:
        if weekday == 6:
            return OpeningHoursResult(
                is_known=True,
                is_open=False,
                status_code="CLOSED_ON_DAY",
                warning="Place is closed on Sundays."
            )

    if "closed mondays" in lower_raw or "closed monday" in lower_raw:
        if weekday == 0:
            return OpeningHoursResult(
                is_known=True,
                is_open=False,
                status_code="CLOSED_ON_DAY",
                warning="Place is closed on Mondays."
            )

    if "closed 2nd & 4th saturdays" in lower_raw and weekday == 5:
        # Check if 2nd or 4th Saturday: 2nd sat is day 8..14, 4th sat is day 22..28
        week_num = (day_of_month - 1) // 7 + 1
        if week_num in (2, 4):
            return OpeningHoursResult(
                is_known=True,
                is_open=False,
                status_code="CLOSED_ON_DAY",
                warning=f"Place is closed on 2nd & 4th Saturdays (day {day_of_month})."
            )

    # 3. Check 24 hours
    if "open 24 hours" in lower_raw or "24/7" in lower_raw or "open 24" in lower_raw:
        return OpeningHoursResult(
            is_known=True,
            is_open=True,
            status_code="CONFIRMED_OPEN",
            open_time_str="00:00",
            close_time_str="23:59",
            open_minutes=0,
            close_minutes=1440
        )

    # 4. Extract time ranges like "10:00 AM – 5:00 PM" or "7:00 AM - 11:00 PM"
    # Normalize dash characters (hyphen, en-dash, em-dash)
    normalized = raw.replace("–", "-").replace("—", "-")
    time_range_match = re.search(
        r"(\d{1,2}(?::\d{2})?\s*(?:AM|PM))\s*-\s*(\d{1,2}(?::\d{2})?\s*(?:AM|PM))",
        normalized,
        re.IGNORECASE
    )

    if not time_range_match:
        # Cannot reliably parse time format
        return OpeningHoursResult(
            is_known=False,
            is_open=False,
            status_code="HOURS_UNKNOWN",
            warning=f"Non-standard opening hours format ('{raw}'). Cannot confirm open times."
        )

    open_str = time_range_match.group(1).strip()
    close_str = time_range_match.group(2).strip()

    open_m = _time_str_to_minutes(open_str)
    close_m = _time_str_to_minutes(close_str)

    if open_m is None or close_m is None:
        return OpeningHoursResult(
            is_known=False,
            is_open=False,
            status_code="HOURS_UNKNOWN",
            warning=f"Could not parse hours: '{raw}'"
        )

    # Handle past-midnight closes (e.g. 12:00 PM to 2:00 AM)
    if close_m <= open_m:
        close_m += 1440

    # Check if arrival and departure fit within open window
    # If departure past 1440 (next day), adjust
    dep_adjusted = departure_minutes
    if dep_adjusted < arrival_minutes:
        dep_adjusted += 1440

    if arrival_minutes >= open_m and dep_adjusted <= close_m:
        return OpeningHoursResult(
            is_known=True,
            is_open=True,
            status_code="CONFIRMED_OPEN",
            open_time_str=open_str,
            close_time_str=close_str,
            open_minutes=open_m,
            close_minutes=close_m
        )
    else:
        return OpeningHoursResult(
            is_known=True,
            is_open=False,
            status_code="OUTSIDE_HOURS",
            warning=f"Visit window ({arrival_minutes//60:02d}:{arrival_minutes%60:02d}–{departure_minutes//60:02d}:{departure_minutes%60:02d}) falls outside operating hours ({open_str} – {close_str}).",
            open_time_str=open_str,
            close_time_str=close_str,
            open_minutes=open_m,
            close_minutes=close_m
        )
