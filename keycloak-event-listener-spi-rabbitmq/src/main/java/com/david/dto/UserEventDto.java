package com.david.dto;

public class UserEventDto {
    private String userId;
    private String username;
    private String email;
    private String eventType;

    public UserEventDto(String userId, String username, String email, String eventType) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.eventType = eventType;
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
