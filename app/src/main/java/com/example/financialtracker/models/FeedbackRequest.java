package com.example.financialtracker.models;

import com.google.gson.annotations.SerializedName;

public class FeedbackRequest {
    @SerializedName("message")
    private String message;

    public FeedbackRequest(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
