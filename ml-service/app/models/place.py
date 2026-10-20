from typing import List, Optional
from pydantic import BaseModel, Field

class MenuItem(BaseModel):
    name: str = Field(..., description="Name of dish or drink")
    price_inr: Optional[int] = Field(None, description="Price in INR if available; None if unknown")
    is_price_available: bool = Field(True, description="True if price is verified and available")
    dietary_type: str = Field("VEGETARIAN", description="VEGETARIAN, VEGAN, NON_VEG, or BEVERAGE")
    description: Optional[str] = Field(None, description="Brief description of dish")
    recommendation_reason: Optional[str] = Field(None, description="Why this dish is recommended")
    source_provenance: Optional[str] = Field("Curated Menu (Verified 2026)", description="Source of menu record")

class Place(BaseModel):
    id: str = Field(..., description="Unique stable place ID, e.g. place_sassoon_docks")
    name: str = Field(..., description="Name of the place")
    description: str = Field(..., description="Full description of the place")
    category: str = Field(..., description="Primary category (Historical, Cultural, Food, Study, Market, Nature, Sunset)")
    area: str = Field(..., description="Neighborhood or area in Mumbai, e.g. Fort, Colaba, Bandra West")
    address: str = Field(..., description="Full postal address")
    estimatedCost: int = Field(0, description="Estimated entry or experience cost in INR")
    priceEstimated: bool = Field(False, description="True if estimatedCost is an estimate rather than exact price")
    durationMinutes: int = Field(60, description="Recommended visit duration in minutes")
    latitude: float = Field(..., description="Latitude coordinate")
    longitude: float = Field(..., description="Longitude coordinate")
    tags: List[str] = Field(default_factory=list, description="Descriptive keywords and subcategories")
    facilities: List[str] = Field(default_factory=list, description="Available facilities, e.g. WiFi, Charging, Seating, AC, Restroom, Quiet")
    verificationStatus: str = Field("VERIFIED", description="VERIFIED, SAMPLE, or COMMUNITY")
    isHiddenGem: bool = Field(False, description="Whether this is a lesser-known curated spot")
    isStudyWork: bool = Field(False, description="Whether suitable for quiet study or remote work")
    isAffordableFood: bool = Field(False, description="Whether this spot offers high-value affordable food")
    cuisineType: Optional[str] = Field("", description="Cuisine type if food establishment")
    quietnessLevel: Optional[str] = Field("Moderate", description="Silent, Moderate, or Lively")
    rating: float = Field(4.5, description="Place rating out of 5.0")
    openingHours: Optional[str] = Field(None, description="Human readable opening hours string")
    insiderTip: Optional[str] = Field(None, description="Curated local tip for visitors")
    ownerId: Optional[str] = Field(None, description="Claimed owner ID in Firestore")
    menu_items: List[MenuItem] = Field(default_factory=list, description="Curated menu items with provenance and dietary info")

    def has_valid_coordinates(self) -> bool:
        # Mumbai latitude is roughly 18.8 to 19.4, longitude is 72.7 to 73.1
        if self.latitude == 0.0 and self.longitude == 0.0:
            return False
        return -90.0 <= self.latitude <= 90.0 and -180.0 <= self.longitude <= 180.0

    def is_mumbai_region(self) -> bool:
        # Check roughly bounded to Greater Mumbai metropolitan area
        return 18.75 <= self.latitude <= 19.45 and 72.70 <= self.longitude <= 73.20
