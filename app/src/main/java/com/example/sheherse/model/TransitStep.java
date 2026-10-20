package com.example.sheherse.model;

import java.io.Serializable;

public class TransitStep implements Serializable {
    private String stepType; // WALK, LOCAL_TRAIN, METRO, TAXI, AUTO
    private String instruction;
    private int durationMinutes;
    private int distanceMeters;
    private String lineName;
    private String boardingStop;
    private String exitStop;
    private int numStops;
    private int fareInr;
    private boolean fareEstimated;
    private String source;

    public TransitStep() {
        this.stepType = "WALK";
    }

    public TransitStep(String stepType, String instruction, int durationMinutes, int distanceMeters,
                       String lineName, String boardingStop, String exitStop, int numStops,
                       int fareInr, boolean fareEstimated, String source) {
        this.stepType = stepType;
        this.instruction = instruction;
        this.durationMinutes = durationMinutes;
        this.distanceMeters = distanceMeters;
        this.lineName = lineName;
        this.boardingStop = boardingStop;
        this.exitStop = exitStop;
        this.numStops = numStops;
        this.fareInr = fareInr;
        this.fareEstimated = fareEstimated;
        this.source = source;
    }

    public String getStepType() {
        return stepType;
    }

    public void setStepType(String stepType) {
        this.stepType = stepType;
    }

    public String getInstruction() {
        return instruction;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public int getDistanceMeters() {
        return distanceMeters;
    }

    public void setDistanceMeters(int distanceMeters) {
        this.distanceMeters = distanceMeters;
    }

    public String getLineName() {
        return lineName;
    }

    public void setLineName(String lineName) {
        this.lineName = lineName;
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

    public int getNumStops() {
        return numStops;
    }

    public void setNumStops(int numStops) {
        this.numStops = numStops;
    }

    public int getFareInr() {
        return fareInr;
    }

    public void setFareInr(int fareInr) {
        this.fareInr = fareInr;
    }

    public boolean isFareEstimated() {
        return fareEstimated;
    }

    public void setFareEstimated(boolean fareEstimated) {
        this.fareEstimated = fareEstimated;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
