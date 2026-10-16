package com.example.sheherse.model;

import java.io.Serializable;

public class Event implements Serializable {

    private String id;
    private String title;
    private String description;
    private String category;
    private String area;
    private String venue;
    private String dateString;
    private String timeString;
    private int price;
    private String hostName;
    private boolean isSample;
    private boolean isSaved;
    private double latitude;
    private double longitude;

    public Event() {
        this.price = 0;
        this.isSample = false;
        this.isSaved = false;
        this.latitude = 0.0;
        this.longitude = 0.0;
    }

    public Event(
            String id,
            String title,
            String description,
            String category,
            String area,
            String venue,
            String dateString,
            String timeString,
            int price,
            String hostName,
            boolean isSample
    ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.area = area;
        this.venue = venue;
        this.dateString = dateString;
        this.timeString = timeString;
        this.price = price;
        this.hostName = hostName;
        this.isSample = isSample;
        this.isSaved = false;
        this.latitude = 0.0;
        this.longitude = 0.0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public String getDateString() {
        return dateString;
    }

    public void setDateString(String dateString) {
        this.dateString = dateString;
    }

    public String getTimeString() {
        return timeString;
    }

    public void setTimeString(String timeString) {
        this.timeString = timeString;
    }

    // Combines the date and time for display.
    public String getDateTime() {
        String date = dateString == null ? "" : dateString.trim();
        String time = timeString == null ? "" : timeString.trim();

        if (date.isEmpty()) {
            return time;
        }

        if (time.isEmpty()) {
            return date;
        }

        return date + " • " + time;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = Math.max(0, price);
    }

    // Compatibility method used by EventAdapter.
    public int getPriceInINR() {
        return price;
    }

    public void setPriceInINR(int price) {
        setPrice(price);
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public boolean isSample() {
        return isSample;
    }

    public void setSample(boolean sample) {
        isSample = sample;
    }

    public boolean isSaved() {
        return isSaved;
    }

    public void setSaved(boolean saved) {
        isSaved = saved;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
