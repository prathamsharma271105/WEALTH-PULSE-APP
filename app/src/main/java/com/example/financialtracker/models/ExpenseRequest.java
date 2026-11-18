package com.example.financialtracker.models;

import com.google.gson.annotations.SerializedName;

public class ExpenseRequest {
    @SerializedName("category")
    private String category;

    @SerializedName("amount")
    private double amount;

    @SerializedName("date")
    private String date;

    @SerializedName("note")
    private String note;

    public ExpenseRequest(String category, double amount, String date, String note) {
        this.category = category;
        this.amount = amount;
        this.date = date;
        this.note = note != null ? note : "";
    }

    public String getCategory() {
        return category;
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
