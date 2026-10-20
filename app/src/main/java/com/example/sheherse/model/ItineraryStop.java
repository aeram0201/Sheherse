package com.example.sheherse.model;

import java.io.Serializable;

public class ItineraryStop implements Serializable {

    private String stopId;
    private Place place;
    private int orderIndex;
    private String customNote;
    private int customDurationMinutes;
    private int customCost;

    // Enhanced scheduling and recommendation fields
    private String arrivalTime;
    private String departureTime;
    private String recommendationReason;
    private int travelTimeToNextMinutes;
    private String travelModeToNext;
    private int travelDistanceToNextMeters;
    private boolean isPriceEstimated;
    private String costNote;
    private String openingStatus;
    private TravelLeg travelLegToNext;

    public ItineraryStop() {
        this.customDurationMinutes = 60;
        this.customCost = 0;
        this.openingStatus = "HOURS_UNKNOWN";
    }

    public ItineraryStop(String stopId, Place place, int orderIndex) {
        this.stopId = stopId;
        this.place = place;
        this.orderIndex = orderIndex;

        this.customDurationMinutes =
                place != null ? place.getDurationMinutes() : 60;

        this.customCost =
                place != null ? place.getEstimatedCost() : 0;

        this.isPriceEstimated =
                place != null && place.isPriceEstimated();

        this.openingStatus = "CONFIRMED_OPEN";
    }

    public String getStopId() {
        return stopId;
    }

    public void setStopId(String stopId) {
        this.stopId = stopId;
    }

    public Place getPlace() {
        return place;
    }

    public void setPlace(Place place) {
        this.place = place;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }

    public String getCustomNote() {
        return customNote;
    }

    public void setCustomNote(String customNote) {
        this.customNote = customNote;
    }

    public int getCustomDurationMinutes() {
        return customDurationMinutes;
    }

    public void setCustomDurationMinutes(int customDurationMinutes) {
        this.customDurationMinutes = customDurationMinutes;
    }

    public int getCustomCost() {
        return customCost;
    }

    public void setCustomCost(int customCost) {
        this.customCost = customCost;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(String arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(String departureTime) {
        this.departureTime = departureTime;
    }

    public String getRecommendationReason() {
        return recommendationReason;
    }

    public void setRecommendationReason(String recommendationReason) {
        this.recommendationReason = recommendationReason;
    }

    public int getTravelTimeToNextMinutes() {
        return travelTimeToNextMinutes;
    }

    public void setTravelTimeToNextMinutes(int travelTimeToNextMinutes) {
        this.travelTimeToNextMinutes = travelTimeToNextMinutes;
    }

    public String getTravelModeToNext() {
        return travelModeToNext;
    }

    public void setTravelModeToNext(String travelModeToNext) {
        this.travelModeToNext = travelModeToNext;
    }

    public int getTravelDistanceToNextMeters() {
        return travelDistanceToNextMeters;
    }

    public void setTravelDistanceToNextMeters(int travelDistanceToNextMeters) {
        this.travelDistanceToNextMeters = travelDistanceToNextMeters;
    }

    public boolean isPriceEstimated() {
        return isPriceEstimated;
    }

    public void setPriceEstimated(boolean priceEstimated) {
        isPriceEstimated = priceEstimated;
    }

    public String getCostNote() {
        return costNote;
    }

    public void setCostNote(String costNote) {
        this.costNote = costNote;
    }

    public String getOpeningStatus() {
        return openingStatus;
    }

    public void setOpeningStatus(String openingStatus) {
        this.openingStatus = openingStatus;
    }

    public TravelLeg getTravelLegToNext() {
        return travelLegToNext;
    }

    public void setTravelLegToNext(TravelLeg travelLegToNext) {
        this.travelLegToNext = travelLegToNext;
    }

    // Convenience methods used by the itinerary adapter.

    public String getPlaceName() {
        return place != null && place.getName() != null
                ? place.getName()
                : "Unknown place";
    }

    public String getPlaceArea() {
        return place != null && place.getArea() != null
                ? place.getArea()
                : "Area not specified";
    }

    public int getDurationMinutes() {
        return customDurationMinutes;
    }

    public int getEstimatedCost() {
        return customCost;
    }
}
