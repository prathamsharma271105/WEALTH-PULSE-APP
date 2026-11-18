package com.example.financialtracker.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class Habit implements Serializable {
    @SerializedName("id")
    private String id;

    @SerializedName("_id")
    private String mongoId;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("name")
    private String name;

    @SerializedName("frequency")
    private String frequency;

    @SerializedName("target_amount")
    private double targetAmount;

    private transient int streak;

    private transient int totalCompletions;

    private transient boolean completedToday;

    private transient List<String> recentLogs;

    @SerializedName("created_at")
    private String createdAt;

    public String getId() {
        return id != null ? id : mongoId;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name != null ? name : "";
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFrequency() {
        return frequency != null ? frequency : "daily";
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public double getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(double targetAmount) {
        this.targetAmount = targetAmount;
    }

    public int getStreak() {
        return streak;
    }

    public void setStreak(int streak) {
        this.streak = streak;
    }

    public int getTotalCompletions() {
        return totalCompletions;
    }

    public void setTotalCompletions(int totalCompletions) {
        this.totalCompletions = totalCompletions;
    }

    public boolean isCompletedToday() {
        return completedToday;
    }

    public void setCompletedToday(boolean completedToday) {
        this.completedToday = completedToday;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setRecentLogs(List<String> recentLogs) {
        this.recentLogs = recentLogs;
    }

    public List<String> getRecentLogs() {
        return recentLogs;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
