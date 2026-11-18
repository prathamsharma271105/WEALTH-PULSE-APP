package com.example.financialtracker.models;

import com.google.gson.annotations.SerializedName;

public class InvestmentRequest {
    @SerializedName("asset_name")
    private String assetName;

    @SerializedName("asset_type")
    private String assetType;

    @SerializedName("amount_invested")
    private double amountInvested;

    @SerializedName("current_value")
    private double currentValue;

    @SerializedName("date")
    private String date;

    public InvestmentRequest(String assetName, String assetType, double amountInvested, double currentValue, String date) {
        this.assetName = assetName;
        this.assetType = assetType;
        this.amountInvested = amountInvested;
        this.currentValue = currentValue;
        this.date = date;
    }

    public String getAssetName() {
        return assetName;
    }

    public String getAssetType() {
        return assetType;
    }

    public double getAmountInvested() {
        return amountInvested;
    }

    public double getCurrentValue() {
        return currentValue;
    }

    public String getDate() {
        return date;
    }
}
