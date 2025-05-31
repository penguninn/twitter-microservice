package com.david.dto;

public class UserEventDto {
    private String userId;
    private String username;
    private String email;
    private String displayName;
    private String profileImgUrl;
    private String eventType;

    public UserEventDto(String displayName, String email, String eventType, String profileImgUrl, String userId, String username) {
        this.displayName = displayName;
        this.email = email;
        this.eventType = eventType;
        this.profileImgUrl = profileImgUrl;
        this.userId = userId;
        this.username = username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getProfileImgUrl() {
        return profileImgUrl;
    }

    public void setProfileImgUrl(String profileImgUrl) {
        this.profileImgUrl = profileImgUrl;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
}
