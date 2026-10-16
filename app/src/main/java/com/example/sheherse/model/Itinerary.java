package com.example.sheherse.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Itinerary implements Serializable {
    private String id;
    private String title;
    private int targetBudget;
    private List<String> interests;
    private List<ItineraryStop> stops;
    private long createdAt;

    public Itinerary() {
        this.interests = new ArrayList<>();
        this.stops = new ArrayList<>();
        this.createdAt = System.currentTimeMillis();
    }

    public Itinerary(String id, String title, int targetBudget, List<String> interests) {
        this.id = id;
        this.title = title;
        this.targetBudget = targetBudget;
        this.interests = interests != null ? interests : new ArrayList<>();
        this.stops = new ArrayList<>();
        this.createdAt = System.currentTimeMillis();
    }

    public int calculateTotalCost() {
        int total = 0;
        if (stops != null) {
            for (ItineraryStop stop : stops) {
                total += stop.getCustomCost();
            }
        }
        return total;
    }

    public int calculateTotalDurationMinutes() {
        int total = 0;
        if (stops != null) {
            for (ItineraryStop stop : stops) {
                total += stop.getCustomDurationMinutes();
            }
        }
        return total;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public int getTargetBudget() { return targetBudget; }
    public void setTargetBudget(int targetBudget) { this.targetBudget = targetBudget; }

    public List<String> getInterests() { return interests; }
    public void setInterests(List<String> interests) { this.interests = interests; }

    public List<ItineraryStop> getStops() { return stops; }
    public void setStops(List<ItineraryStop> stops) { this.stops = stops; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
