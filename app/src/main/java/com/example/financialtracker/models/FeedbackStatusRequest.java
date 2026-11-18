package com.example.financialtracker.models;

import com.google.gson.annotations.SerializedName;

public class FeedbackStatusRequest {
    @SerializedName("status")
    private String status;

    public FeedbackStatusRequest(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
