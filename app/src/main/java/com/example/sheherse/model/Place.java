package com.example.sheherse.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Place implements Serializable {
    private String id;
    private String name;
    private String description;
    private String category; // Historical, Nature, Cultural, Food, Study, Market, Sunset
    private String area; // South Mumbai, Fort, Colaba, Bandra, Dadar, Matunga, etc.
    private String address;
    private int estimatedCost; // in INR
    private boolean priceEstimated;
    private int durationMinutes;
    private double latitude;
    private double longitude;
    private List<String> tags;
    private List<String> facilities; // WiFi, Charging, Seating, AC, Restroom, Quiet
    private String verificationStatus; // SAMPLE, COMMUNITY, VERIFIED
    private boolean isHiddenGem;
    private boolean isStudyWork;
    private boolean isAffordableFood;
    private String cuisineType; // Vada Pav & Chaat, Parsi/Irani, South Indian, Coastal/Seafood, Cafe
    private String quietnessLevel; // Silent, Moderate, Lively
    private float rating;
    private String openingHours;
    private String insiderTip;
    private String ownerId;

    public Place() {
        this.tags = new ArrayList<>();
        this.facilities = new ArrayList<>();
    }

    public Place(String id, String name, String description, String category, String area,
                 String address, int estimatedCost, boolean priceEstimated, int durationMinutes,
                 double latitude, double longitude, List<String> tags, List<String> facilities,
                 String verificationStatus, boolean isHiddenGem, boolean isStudyWork,
                 boolean isAffordableFood, String cuisineType, String quietnessLevel,
                 float rating, String openingHours, String insiderTip) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.area = area;
        this.address = address;
        this.estimatedCost = estimatedCost;
        this.priceEstimated = priceEstimated;
        this.durationMinutes = durationMinutes;
        this.latitude = latitude;
        this.longitude = longitude;
        this.tags = tags != null ? tags : new ArrayList<>();
        this.facilities = facilities != null ? facilities : new ArrayList<>();
        this.verificationStatus = verificationStatus;
        this.isHiddenGem = isHiddenGem;
        this.isStudyWork = isStudyWork;
        this.isAffordableFood = isAffordableFood;
        this.cuisineType = cuisineType;
        this.quietnessLevel = quietnessLevel;
        this.rating = rating;
        this.openingHours = openingHours;
        this.insiderTip = insiderTip;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public int getEstimatedCost() { return estimatedCost; }
    public void setEstimatedCost(int estimatedCost) { this.estimatedCost = estimatedCost; }

    public boolean isPriceEstimated() { return priceEstimated; }
    public void setPriceEstimated(boolean priceEstimated) { this.priceEstimated = priceEstimated; }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public List<String> getFacilities() { return facilities; }
    public void setFacilities(List<String> facilities) { this.facilities = facilities; }

    public String getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(String verificationStatus) { this.verificationStatus = verificationStatus; }

    public boolean isHiddenGem() { return isHiddenGem; }
    public void setHiddenGem(boolean hiddenGem) { isHiddenGem = hiddenGem; }

    public boolean isStudyWork() { return isStudyWork; }
    public void setStudyWork(boolean studyWork) { isStudyWork = studyWork; }

    public boolean isAffordableFood() { return isAffordableFood; }
    public void setAffordableFood(boolean affordableFood) { isAffordableFood = affordableFood; }

    public String getCuisineType() { return cuisineType; }
    public void setCuisineType(String cuisineType) { this.cuisineType = cuisineType; }

    public String getQuietnessLevel() { return quietnessLevel; }
    public void setQuietnessLevel(String quietnessLevel) { this.quietnessLevel = quietnessLevel; }

    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }

    public String getOpeningHours() { return openingHours; }
    public void setOpeningHours(String openingHours) { this.openingHours = openingHours; }

    public String getInsiderTip() { return insiderTip; }
    public void setInsiderTip(String insiderTip) { this.insiderTip = insiderTip; }

    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
}
