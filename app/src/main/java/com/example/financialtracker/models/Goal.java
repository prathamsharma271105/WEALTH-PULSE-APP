package com.example.financialtracker.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Goal implements Serializable {
    @SerializedName("id")
    private String id;

    @SerializedName("_id")
    private String mongoId;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("title")
    private String title;

    @SerializedName(value = "target_amount", alternate = {"targetAmount", "target"})
    private Double targetAmount;

    @SerializedName(value = "saved_amount", alternate = {"savedAmount", "saved", "current_amount", "currentAmount"})
    private Double savedAmount;

    @SerializedName("deadline")
    private String deadline;

    private transient int progressPercent;

    @SerializedName("created_at")
    private String createdAt;

    public String getId() {
        return id != null ? id : mongoId;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title != null ? title : "";
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getTargetAmount() {
        return targetAmount != null ? targetAmount : 0.0;
    }

    public void setTargetAmount(double targetAmount) {
        this.targetAmount = targetAmount;
    }

    public double getSavedAmount() {
        return savedAmount != null ? savedAmount : 0.0;
    }

    public void setSavedAmount(double savedAmount) {
        this.savedAmount = savedAmount;
    }

    public String getDeadline() {
        return deadline != null ? deadline : "";
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public int getProgressPercent() {
        if (progressPercent > 0) return progressPercent;
        double tgt = getTargetAmount();
        if (tgt <= 0) return 0;
        return (int) Math.min(Math.round((getSavedAmount() / tgt) * 100), 100);
    }

    public double getProgressPercentageDouble() {
        double tgt = getTargetAmount();
        if (tgt <= 0) return 0.0;
        return Math.min(100.0, (getSavedAmount() / tgt) * 100.0);
    }

    public void setProgressPercent(int progressPercent) {
        this.progressPercent = progressPercent;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
