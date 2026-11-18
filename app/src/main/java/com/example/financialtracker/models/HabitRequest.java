package com.example.financialtracker.models;

import com.google.gson.annotations.SerializedName;

public class HabitRequest {
    @SerializedName("name")
    private String name;

    @SerializedName("frequency")
    private String frequency;

    @SerializedName("target_amount")
    private double targetAmount;

    public HabitRequest(String name, String frequency, double targetAmount) {
        this.name = name;
        this.frequency = frequency;
        this.targetAmount = targetAmount;
    }

    public String getName() {
        return name;
    }

    public String getFrequency() {
        return frequency;
    }

    public double getTargetAmount() {
        return targetAmount;
    }
}
