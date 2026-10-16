package com.example.sheherse.model;

import java.io.Serializable;

public class CommunitySubmission implements Serializable {
    public static final String TYPE_PLACE = "PLACE";
    public static final String TYPE_TIP = "TIP";
    public static final String TYPE_EVENT = "EVENT";
    public static final String TYPE_REPORT = "REPORT";

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_REJECTED = "REJECTED";

    private String id;
    private String userId;
    private String authorName;
    private String type; // PLACE, TIP, EVENT, REPORT
    private String title;
    private String description;
    private String area;
    private String category;
    private String status; // PENDING, APPROVED, REJECTED
    private int upvotes;
    private long timestamp;

    public CommunitySubmission() {
        this.timestamp = System.currentTimeMillis();
        this.status = STATUS_PENDING;
        this.upvotes = 1;
    }

    public CommunitySubmission(String id, String userId, String authorName, String type,
                               String title, String description, String area, String category,
                               String status, int upvotes) {
        this.id = id;
        this.userId = userId;
        this.authorName = authorName;
        this.type = type;
        this.title = title;
        this.description = description;
        this.area = area;
        this.category = category;
        this.status = status;
        this.upvotes = upvotes;
        this.timestamp = System.currentTimeMillis();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getUpvotes() { return upvotes; }
    public void setUpvotes(int upvotes) { this.upvotes = upvotes; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
