package com.example.financialtracker.models;

import com.google.gson.annotations.SerializedName;

public class UpdateProfileRequest {
    @SerializedName("name")
    private String name;

    @SerializedName("email")
    private String email;

    @SerializedName("phone")
    private String phone;

    @SerializedName("currency")
    private String currency;

    @SerializedName("income_target")
    private double incomeTarget;

    @SerializedName("savings_target")
    private double savingsTarget;

    public UpdateProfileRequest(String name, String email, String phone, String currency, double incomeTarget, double savingsTarget) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.currency = currency;
        this.incomeTarget = incomeTarget;
        this.savingsTarget = savingsTarget;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getCurrency() {
        return currency;
    }

    public double getIncomeTarget() {
        return incomeTarget;
    }

    public double getSavingsTarget() {
        return savingsTarget;
    }
}
