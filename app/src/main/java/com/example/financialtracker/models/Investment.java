package com.example.financialtracker.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Investment implements Serializable {
    @SerializedName("id")
    private String id;

    @SerializedName("_id")
    private String mongoId;

    @SerializedName("user_id")
    private String userId;

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

    @SerializedName("created_at")
    private String createdAt;

    public String getId() {
        return id != null ? id : mongoId;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAssetName() {
        return assetName != null ? assetName : "";
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }

    public String getAssetType() {
        return assetType != null ? assetType : "Other";
    }

    public void setAssetType(String assetType) {
        this.assetType = assetType;
    }

    public double getAmountInvested() {
        return amountInvested;
    }

    public void setAmountInvested(double amountInvested) {
        this.amountInvested = amountInvested;
    }

    public double getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(double currentValue) {
        this.currentValue = currentValue;
    }

    public double getProfitLoss() {
        return currentValue - amountInvested;
    }

    public double getProfitLossPercentage() {
        if (amountInvested <= 0) return 0.0;
        return ((currentValue - amountInvested) / amountInvested) * 100.0;
    }

    public String getDate() {
        return date != null ? date : "";
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
