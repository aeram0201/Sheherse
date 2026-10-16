package com.example.sheherse.model;

import java.io.Serializable;

public class Enquiry implements Serializable {

    public static final String TARGET_TOUR = "TOUR";
    public static final String TARGET_PLACE = "PLACE";

    private String id;
    private String targetType; // TOUR, PLACE
    private String targetId;
    private String targetTitle;
    private String recipientId; // guideId or ownerId
    private String senderName;
    private String senderContact;
    private String message;
    private String preferredDate;
    private int partySize;
    private long timestamp;

    public Enquiry() {
        this.timestamp = System.currentTimeMillis();
        this.partySize = 1;
    }

    public Enquiry(
            String id,
            String targetType,
            String targetId,
            String targetTitle,
            String recipientId,
            String senderName,
            String senderContact,
            String message,
            String preferredDate,
            int partySize
    ) {
        this.id = id;
        this.targetType = targetType;
        this.targetId = targetId;
        this.targetTitle = targetTitle;
        this.recipientId = recipientId;
        this.senderName = senderName;
        this.senderContact = senderContact;
        this.message = message;
        this.preferredDate = preferredDate;
        this.partySize = partySize;
        this.timestamp = System.currentTimeMillis();
    }

    public Enquiry(
            String id,
            String recipientId,
            String senderId,
            String senderName,
            String email,
            String phone,
            String preferredDate,
            int partySize,
            String message
    ) {
        this(id, TARGET_PLACE, "", "Enquiry / Booking", recipientId, senderName, phone != null && !phone.isEmpty() ? phone : email, message, preferredDate, partySize);
    }

    public Enquiry(
            String id,
            String recipientId,
            String senderName,
            String email,
            String phone,
            String preferredDate,
            int partySize,
            String message
    ) {
        this(id, TARGET_TOUR, "", "Heritage Trail Walk", recipientId, senderName, phone != null && !phone.isEmpty() ? phone : email, message, preferredDate, partySize);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public String getTargetTitle() {
        return targetTitle;
    }

    public void setTargetTitle(String targetTitle) {
        this.targetTitle = targetTitle;
    }

    public String getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderContact() {
        return senderContact;
    }

    public void setSenderContact(String senderContact) {
        this.senderContact = senderContact;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPreferredDate() {
        return preferredDate;
    }

    public void setPreferredDate(String preferredDate) {
        this.preferredDate = preferredDate;
    }

    public int getPartySize() {
        return partySize;
    }

    public void setPartySize(int partySize) {
        this.partySize = partySize;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    // Compatibility method for adapters using getCreatedAt()
    public String getCreatedAt() {
        java.text.SimpleDateFormat dateFormat =
                new java.text.SimpleDateFormat(
                        "dd MMM yyyy",
                        java.util.Locale.getDefault()
                );

        return dateFormat.format(new java.util.Date(timestamp));
    }
}
