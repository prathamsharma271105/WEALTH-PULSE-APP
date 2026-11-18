package com.example.financialtracker.models;

import com.google.gson.annotations.SerializedName;

public class RegisterRequest {
    @SerializedName("name")
    private String name;

    @SerializedName("email")
    private String email;

    @SerializedName("password")
    private String password;

    @SerializedName("currency")
    private String currency;

    @SerializedName("phone")
    private String phone;

    @SerializedName("income_target")
    private double incomeTarget;

    @SerializedName("savings_target")
    private double savingsTarget;

    public RegisterRequest(String name, String email, String password, String currency, String phone, double incomeTarget, double savingsTarget) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.currency = currency;
        this.phone = phone;
        this.incomeTarget = incomeTarget;
        this.savingsTarget = savingsTarget;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getCurrency() {
        return currency;
    }

    public String getPhone() {
        return phone;
    }

    public double getIncomeTarget() {
        return incomeTarget;
    }

    public double getSavingsTarget() {
        return savingsTarget;
    }
}
