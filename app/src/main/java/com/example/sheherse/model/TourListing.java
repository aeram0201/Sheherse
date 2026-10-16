package com.example.sheherse.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TourListing implements Serializable {
    private String id;
    private String guideId;
    private String guideName;
    private String title;
    private String description;
    private int pricePerPerson;
    private int durationHours;
    private String meetingPoint;
    private List<String> languages;
    private String area;
    private float rating;

    public TourListing() {
        this.languages = new ArrayList<>();
    }

    public TourListing(String id, String guideId, String guideName, String title,
                       String description, int pricePerPerson, int durationHours,
                       String meetingPoint, List<String> languages, String area, float rating) {
        this.id = id;
        this.guideId = guideId;
        this.guideName = guideName;
        this.title = title;
        this.description = description;
        this.pricePerPerson = pricePerPerson;
        this.durationHours = durationHours;
        this.meetingPoint = meetingPoint;
        this.languages = languages != null ? languages : new ArrayList<>();
        this.area = area;
        this.rating = rating;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getGuideId() { return guideId; }
    public void setGuideId(String guideId) { this.guideId = guideId; }

    public String getGuideName() { return guideName; }
    public void setGuideName(String guideName) { this.guideName = guideName; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getPricePerPerson() { return pricePerPerson; }
    public void setPricePerPerson(int pricePerPerson) { this.pricePerPerson = pricePerPerson; }

    public int getDurationHours() { return durationHours; }
    public void setDurationHours(int durationHours) { this.durationHours = durationHours; }

    public String getMeetingPoint() { return meetingPoint; }
    public void setMeetingPoint(String meetingPoint) { this.meetingPoint = meetingPoint; }

    public List<String> getLanguages() { return languages; }
    public void setLanguages(List<String> languages) { this.languages = languages; }

    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }

    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }
}
