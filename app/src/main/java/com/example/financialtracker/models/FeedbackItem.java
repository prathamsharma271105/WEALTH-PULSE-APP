package com.example.financialtracker.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class FeedbackItem implements Serializable {
    @SerializedName("id")
    private String id;

    @SerializedName("_id")
    private String mongoId;

    @SerializedName("user_id")
    private Object userId;

    @SerializedName("message")
    private String message;

    @SerializedName("status")
    private String status;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("user_name")
    private String userName;

    @SerializedName("user_email")
    private String userEmail;

    public String getId() {
        return id != null ? id : mongoId;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMessage() {
        return message != null ? message : "";
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status != null ? status : "open";
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isResolved() {
        return "resolved".equalsIgnoreCase(status);
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUserName() {
        return userName != null ? userName : "User";
    }

    public void setUserId(Object userId) {
        this.userId = userId;
    }

    public String getUserEmail() {
        return userEmail != null ? userEmail : "";
    }
}
