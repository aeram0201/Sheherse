package com.example.sheherse.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TravelLeg implements Serializable {
    private String fromTitle;
    private String toTitle;
    private String fromPlaceId;
    private String toPlaceId;
    private String mode; // WALKING, TRANSIT, DRIVING, TWO_WHEELER
    private String recommendedMode; // WALKING, LOCAL_TRAIN, METRO, TAXI_CAB, AUTO_RICKSHAW
    private int distanceMeters;
    private int durationMinutes;
    private boolean isEstimated;
    private String estimationBasis;
    private String routeWarning;

    // Enhanced transit details
    private boolean transitAvailable;
    private String transitSummary;
    private String boardingStop;
    private String exitStop;
    private String lineOrRoute;
    private int walkingDistanceMeters;
    private int walkingDurationMinutes;
    private int fareInr;
    private boolean fareIsEstimated;
    private String fareSource;
    private List<TransitStep> steps;

    public TravelLeg() {
        this.steps = new ArrayList<>();
        this.transitAvailable = true;
        this.recommendedMode = "WALKING";
    }

    public TravelLeg(String fromTitle, String toTitle, String fromPlaceId, String toPlaceId,
                     String mode, int distanceMeters, int durationMinutes) {
        this.fromTitle = fromTitle;
        this.toTitle = toTitle;
        this.fromPlaceId = fromPlaceId;
        this.toPlaceId = toPlaceId;
        this.mode = mode;
        this.distanceMeters = distanceMeters;
        this.durationMinutes = durationMinutes;
        this.steps = new ArrayList<>();
        this.transitAvailable = true;
        this.recommendedMode = mode != null ? mode : "WALKING";
    }

    public String getFromTitle() {
        return fromTitle;
    }

    public void setFromTitle(String fromTitle) {
        this.fromTitle = fromTitle;
    }

    public String getToTitle() {
        return toTitle;
    }

    public void setToTitle(String toTitle) {
        this.toTitle = toTitle;
    }

    public String getFromPlaceId() {
        return fromPlaceId;
    }

    public void setFromPlaceId(String fromPlaceId) {
        this.fromPlaceId = fromPlaceId;
    }

    public String getToPlaceId() {
        return toPlaceId;
    }

    public void setToPlaceId(String toPlaceId) {
        this.toPlaceId = toPlaceId;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getRecommendedMode() {
        return recommendedMode;
    }

    public void setRecommendedMode(String recommendedMode) {
        this.recommendedMode = recommendedMode;
    }

    public int getDistanceMeters() {
        return distanceMeters;
    }

    public void setDistanceMeters(int distanceMeters) {
        this.distanceMeters = distanceMeters;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public boolean isEstimated() {
        return isEstimated;
    }

    public void setEstimated(boolean estimated) {
        isEstimated = estimated;
    }

    public String getEstimationBasis() {
        return estimationBasis;
    }

    public void setEstimationBasis(String estimationBasis) {
        this.estimationBasis = estimationBasis;
    }

    public String getRouteWarning() {
        return routeWarning;
    }

    public void setRouteWarning(String routeWarning) {
        this.routeWarning = routeWarning;
    }

    public boolean isTransitAvailable() {
        return transitAvailable;
    }

    public void setTransitAvailable(boolean transitAvailable) {
        this.transitAvailable = transitAvailable;
    }

    public String getTransitSummary() {
        return transitSummary;
    }

    public void setTransitSummary(String transitSummary) {
        this.transitSummary = transitSummary;
    }

    public String getBoardingStop() {
        return boardingStop;
    }

    public void setBoardingStop(String boardingStop) {
        this.boardingStop = boardingStop;
    }

    public String getExitStop() {
        return exitStop;
    }

    public void setExitStop(String exitStop) {
        this.exitStop = exitStop;
    }

    public String getLineOrRoute() {
        return lineOrRoute;
    }

    public void setLineOrRoute(String lineOrRoute) {
        this.lineOrRoute = lineOrRoute;
    }

    public int getWalkingDistanceMeters() {
        return walkingDistanceMeters;
    }

    public void setWalkingDistanceMeters(int walkingDistanceMeters) {
        this.walkingDistanceMeters = walkingDistanceMeters;
    }

    public int getWalkingDurationMinutes() {
        return walkingDurationMinutes;
    }

    public void setWalkingDurationMinutes(int walkingDurationMinutes) {
        this.walkingDurationMinutes = walkingDurationMinutes;
    }

    public int getFareInr() {
        return fareInr;
    }

    public void setFareInr(int fareInr) {
        this.fareInr = fareInr;
    }

    public boolean isFareIsEstimated() {
        return fareIsEstimated;
    }

    public void setFareIsEstimated(boolean fareIsEstimated) {
        this.fareIsEstimated = fareIsEstimated;
    }

    public String getFareSource() {
        return fareSource;
    }

    public void setFareSource(String fareSource) {
        this.fareSource = fareSource;
    }

    public List<TransitStep> getSteps() {
        return steps != null ? steps : new ArrayList<>();
    }

    public void setSteps(List<TransitStep> steps) {
        this.steps = steps;
    }
}
