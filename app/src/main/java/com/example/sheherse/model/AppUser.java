package com.example.sheherse.model;

import java.io.Serializable;

public class AppUser implements Serializable {
    public static final String ROLE_EXPLORER = "EXPLORER";
    public static final String ROLE_RESIDENT = "RESIDENT";
    public static final String ROLE_STUDENT = "STUDENT";
    public static final String ROLE_GUIDE = "GUIDE";
    public static final String ROLE_OWNER = "OWNER";
    public static final String ROLE_ORGANIZER = "ORGANIZER";

    private String id;
    private String name;
    private String email;
    private String role; // EXPLORER, RESIDENT, STUDENT, GUIDE, OWNER, ORGANIZER
    private boolean isDemo;
    private String avatarInitials;
    private String bio;

    public AppUser() {}

    public AppUser(String id, String name, String email, String role, boolean isDemo, String bio) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role != null ? role : ROLE_EXPLORER;
        this.isDemo = isDemo;
        this.bio = bio;
        this.avatarInitials = calculateInitials(name);
    }

    private String calculateInitials(String name) {
        if (name == null || name.trim().isEmpty()) return "SH";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        } else {
            return ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { 
        this.name = name; 
        this.avatarInitials = calculateInitials(name);
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getRoleDisplayName() {
        if (ROLE_GUIDE.equalsIgnoreCase(role)) return "Heritage Tour Guide";
        if (ROLE_OWNER.equalsIgnoreCase(role)) return "Local Business Owner";
        if (ROLE_STUDENT.equalsIgnoreCase(role)) return "Student / Freelancer";
        if (ROLE_RESIDENT.equalsIgnoreCase(role)) return "Mumbai Resident";
        if (ROLE_ORGANIZER.equalsIgnoreCase(role)) return "Event Organizer";
        return "Explorer / Tourist";
    }

    public boolean isDemo() { return isDemo; }
    public void setDemo(boolean demo) { isDemo = demo; }

    public String getAvatarInitials() { 
        if (avatarInitials == null || avatarInitials.isEmpty()) {
            return calculateInitials(name);
        }
        return avatarInitials; 
    }
    public void setAvatarInitials(String avatarInitials) { this.avatarInitials = avatarInitials; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
}
