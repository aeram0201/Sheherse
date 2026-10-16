package com.example.sheherse.model;

import java.io.Serializable;

public class ItineraryStop implements Serializable {

    private String stopId;
    private Place place;
    private int orderIndex;
    private String customNote;
    private int customDurationMinutes;
    private int customCost;

    public ItineraryStop() {
        this.customDurationMinutes = 60;
        this.customCost = 0;
    }

    public ItineraryStop(String stopId, Place place, int orderIndex) {
        this.stopId = stopId;
        this.place = place;
        this.orderIndex = orderIndex;

        this.customDurationMinutes =
                place != null ? place.getDurationMinutes() : 60;

        this.customCost =
                place != null ? place.getEstimatedCost() : 0;
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
