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

    // Detailed planning fields
    private String planningDate;
    private String startTime;
    private String finishTime;
    private int totalTravelMinutes;
    private int totalVisitMinutes;
    private boolean costIsFullyKnown;
    private String planningRationale;
    private List<String> warnings;
    private boolean isFeasible;
    private List<String> infeasibilityReasons;
    private List<String> alternativeSuggestions;
    private List<TravelLeg> travelLegs;
    private CostBreakdown costBreakdown;

    public Itinerary() {
        this.interests = new ArrayList<>();
        this.stops = new ArrayList<>();
        this.travelLegs = new ArrayList<>();
        this.warnings = new ArrayList<>();
        this.infeasibilityReasons = new ArrayList<>();
        this.alternativeSuggestions = new ArrayList<>();
        this.costIsFullyKnown = true;
        this.isFeasible = true;
        this.createdAt = System.currentTimeMillis();
    }

    public Itinerary(String id, String title, int targetBudget, List<String> interests) {
        this.id = id;
        this.title = title;
        this.targetBudget = targetBudget;
        this.interests = interests != null ? interests : new ArrayList<>();
        this.stops = new ArrayList<>();
        this.travelLegs = new ArrayList<>();
        this.warnings = new ArrayList<>();
        this.infeasibilityReasons = new ArrayList<>();
        this.alternativeSuggestions = new ArrayList<>();
        this.costIsFullyKnown = true;
        this.isFeasible = true;
        this.createdAt = System.currentTimeMillis();
    }

    public int calculateTotalCost() {
        if (costBreakdown != null && costBreakdown.getTotalCostInr() > 0) {
            return costBreakdown.getTotalCostInr();
        }
        int total = 0;
        if (stops != null) {
            for (ItineraryStop stop : stops) {
                total += stop.getCustomCost();
            }
        }
        if (travelLegs != null) {
            for (TravelLeg leg : travelLegs) {
                total += leg.getFareInr();
            }
        }
        return total;
    }

    public int calculateTotalDurationMinutes() {
        int total = 0;
        if (stops != null) {
            for (ItineraryStop stop : stops) {
                total += stop.getCustomDurationMinutes();
                total += stop.getTravelTimeToNextMinutes();
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

    public String getPlanningDate() { return planningDate; }
    public void setPlanningDate(String planningDate) { this.planningDate = planningDate; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getFinishTime() { return finishTime; }
    public void setFinishTime(String finishTime) { this.finishTime = finishTime; }

    public int getTotalTravelMinutes() { return totalTravelMinutes; }
    public void setTotalTravelMinutes(int totalTravelMinutes) { this.totalTravelMinutes = totalTravelMinutes; }

    public int getTotalVisitMinutes() { return totalVisitMinutes; }
    public void setTotalVisitMinutes(int totalVisitMinutes) { this.totalVisitMinutes = totalVisitMinutes; }

    public boolean isCostIsFullyKnown() { return costIsFullyKnown; }
    public void setCostIsFullyKnown(boolean costIsFullyKnown) { this.costIsFullyKnown = costIsFullyKnown; }

    public String getPlanningRationale() { return planningRationale; }
    public void setPlanningRationale(String planningRationale) { this.planningRationale = planningRationale; }

    public List<String> getWarnings() { return warnings; }
    public void setWarnings(List<String> warnings) { this.warnings = warnings; }

    public boolean isFeasible() { return isFeasible; }
    public void setFeasible(boolean feasible) { isFeasible = feasible; }

    public List<String> getInfeasibilityReasons() { return infeasibilityReasons; }
    public void setInfeasibilityReasons(List<String> infeasibilityReasons) { this.infeasibilityReasons = infeasibilityReasons; }

    public List<String> getAlternativeSuggestions() { return alternativeSuggestions; }
    public void setAlternativeSuggestions(List<String> alternativeSuggestions) { this.alternativeSuggestions = alternativeSuggestions; }

    public List<TravelLeg> getTravelLegs() { return travelLegs != null ? travelLegs : new ArrayList<>(); }
    public void setTravelLegs(List<TravelLeg> travelLegs) { this.travelLegs = travelLegs; }

    public CostBreakdown getCostBreakdown() { return costBreakdown; }
    public void setCostBreakdown(CostBreakdown costBreakdown) { this.costBreakdown = costBreakdown; }
}
