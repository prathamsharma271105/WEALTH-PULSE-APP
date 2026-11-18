package com.example.financialtracker.models;

import com.google.gson.annotations.SerializedName;

public class GoalRequest {
    @SerializedName("title")
    private String title;

    @SerializedName(value = "target_amount", alternate = {"targetAmount", "target"})
    private double targetAmount;

    @SerializedName(value = "saved_amount", alternate = {"savedAmount", "saved"})
    private double savedAmount;

    @SerializedName("deadline")
    private String deadline;

    public GoalRequest(String title, double targetAmount, double savedAmount, String deadline) {
        this.title = title;
        this.targetAmount = targetAmount;
        this.savedAmount = savedAmount;
        this.deadline = deadline;
    }

    public String getTitle() {
        return title;
    }

    public double getTargetAmount() {
        return targetAmount;
    }

    public double getSavedAmount() {
        return savedAmount;
    }

    public String getDeadline() {
        return deadline;
    }
}
