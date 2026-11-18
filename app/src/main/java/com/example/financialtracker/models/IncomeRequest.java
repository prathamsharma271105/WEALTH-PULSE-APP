package com.example.financialtracker.models;

import com.google.gson.annotations.SerializedName;

public class IncomeRequest {
    @SerializedName("source")
    private String source;

    @SerializedName("amount")
    private double amount;

    @SerializedName("date")
    private String date;

    @SerializedName("note")
    private String note;

    public IncomeRequest(String source, double amount, String date, String note) {
        this.source = source;
        this.amount = amount;
        this.date = date;
        this.note = note != null ? note : "";
    }

    public String getSource() {
        return source;
    }

    public double getAmount() {
        return amount;
    }

    public String getDate() {
        return date;
    }

    public String getNote() {
        return note;
    }
}
