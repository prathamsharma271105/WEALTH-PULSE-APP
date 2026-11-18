package com.example.financialtracker.models;

import com.google.gson.annotations.SerializedName;

public class InvestmentUpdateRequest {
    @SerializedName("current_value")
    private double currentValue;

    public InvestmentUpdateRequest(double currentValue) {
        this.currentValue = currentValue;
    }

    public double getCurrentValue() {
        return currentValue;
    }
}
