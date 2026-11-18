package com.example.financialtracker.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Income implements Serializable {
    @SerializedName("id")
    private String id;

    @SerializedName("_id")
    private String mongoId;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("source")
    private String source;

    @SerializedName("amount")
    private double amount;

    @SerializedName("date")
    private String date;

    @SerializedName("note")
    private String note;

    @SerializedName("created_at")
    private String createdAt;

    public String getId() {
        return id != null ? id : mongoId;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSource() {
        return source != null ? source : "";
    }

    public void setSource(String source) {
        this.source = source;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDate() {
        return date != null ? date : "";
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getNote() {
        return note != null ? note : "";
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
