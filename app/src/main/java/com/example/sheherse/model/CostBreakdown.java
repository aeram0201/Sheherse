package com.example.sheherse.model;

import java.io.Serializable;

public class CostBreakdown implements Serializable {
    private int entryFeesInr;
    private int foodCostInr;
    private int transportFareInr;
    private int totalCostInr;
    private boolean hasUnknownCosts;
    private String costCompletenessNote;

    public CostBreakdown() {
        this.costCompletenessNote = "All costs sourced";
    }

    public CostBreakdown(int entryFeesInr, int foodCostInr, int transportFareInr,
                         int totalCostInr, boolean hasUnknownCosts, String costCompletenessNote) {
        this.entryFeesInr = entryFeesInr;
        this.foodCostInr = foodCostInr;
        this.transportFareInr = transportFareInr;
        this.totalCostInr = totalCostInr;
        this.hasUnknownCosts = hasUnknownCosts;
        this.costCompletenessNote = costCompletenessNote;
    }

    public int getEntryFeesInr() {
        return entryFeesInr;
    }

    public void setEntryFeesInr(int entryFeesInr) {
        this.entryFeesInr = entryFeesInr;
    }

    public int getFoodCostInr() {
        return foodCostInr;
    }

    public void setFoodCostInr(int foodCostInr) {
        this.foodCostInr = foodCostInr;
    }

    public int getTransportFareInr() {
        return transportFareInr;
    }

    public void setTransportFareInr(int transportFareInr) {
        this.transportFareInr = transportFareInr;
    }

    public int getTotalCostInr() {
        return totalCostInr;
    }

    public void setTotalCostInr(int totalCostInr) {
        this.totalCostInr = totalCostInr;
    }

    public boolean isHasUnknownCosts() {
        return hasUnknownCosts;
    }

    public void setHasUnknownCosts(boolean hasUnknownCosts) {
        this.hasUnknownCosts = hasUnknownCosts;
    }

    public String getCostCompletenessNote() {
        return costCompletenessNote;
    }

    public void setCostCompletenessNote(String costCompletenessNote) {
        this.costCompletenessNote = costCompletenessNote;
    }
}
