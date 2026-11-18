package com.example.financialtracker.models;

import com.google.gson.annotations.SerializedName;

public class ApiResponse {
    @SerializedName("ok")
    private boolean ok;

    @SerializedName("message")
    private String message;

    @SerializedName("error")
    private String error;

    public ApiResponse() {}

    public ApiResponse(String message, boolean ok) {
        this.message = message;
        this.ok = ok;
    }

    public boolean isOk() {
        return ok;
    }

    public String getMessage() {
        return message;
    }

    public String getError() {
        return error;
    }
}
