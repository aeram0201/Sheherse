package com.example.sheherse.model;

import java.io.Serializable;

public class MenuItem implements Serializable {
    private String name;
    private int priceInr;
    private boolean priceAvailable;
    private String dietaryType; // VEGETARIAN, NON_VEG, VEGAN, BEVERAGE
    private String description;
    private String recommendationReason;
    private String sourceProvenance;

    public MenuItem() {
        this.priceAvailable = true;
        this.dietaryType = "VEGETARIAN";
    }

    public MenuItem(String name, int priceInr, boolean priceAvailable, String dietaryType,
                    String description, String recommendationReason, String sourceProvenance) {
        this.name = name;
        this.priceInr = priceInr;
        this.priceAvailable = priceAvailable;
        this.dietaryType = dietaryType != null ? dietaryType : "VEGETARIAN";
        this.description = description;
        this.recommendationReason = recommendationReason;
        this.sourceProvenance = sourceProvenance;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPriceInr() {
        return priceInr;
    }

    public void setPriceInr(int priceInr) {
        this.priceInr = priceInr;
    }

    public boolean isPriceAvailable() {
        return priceAvailable;
    }

    public void setPriceAvailable(boolean priceAvailable) {
        this.priceAvailable = priceAvailable;
    }

    public String getDietaryType() {
        return dietaryType;
    }

    public void setDietaryType(String dietaryType) {
        this.dietaryType = dietaryType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRecommendationReason() {
        return recommendationReason;
    }

    public void setRecommendationReason(String recommendationReason) {
        this.recommendationReason = recommendationReason;
    }

    public String getSourceProvenance() {
        return sourceProvenance;
    }

    public void setSourceProvenance(String sourceProvenance) {
        this.sourceProvenance = sourceProvenance;
    }
}
