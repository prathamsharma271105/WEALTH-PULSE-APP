package com.example.financialtracker.models;

import com.google.gson.annotations.SerializedName;

public class GoalContributeRequest {
    @SerializedName("amount")
    private double amount;

    public GoalContributeRequest(double amount) {
        this.amount = amount;
    }

    public double getAmount() {
        return amount;
    }
}
