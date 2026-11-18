package com.example.financialtracker.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class User implements Serializable {
    @SerializedName("id")
    private String id;

    @SerializedName("_id")
    private String mongoId;

    @SerializedName("name")
    private String name;

    @SerializedName("email")
    private String email;

    @SerializedName("role")
    private String role;

    @SerializedName("password_hash")
    private String passwordHash;

    @SerializedName("currency")
    private String currency;

    @SerializedName("phone")
    private String phone;

    @SerializedName("income_target")
    private double incomeTarget;

    @SerializedName("savings_target")
    private double savingsTarget;

    @SerializedName("created_at")
    private String createdAt;

    public String getId() {
        return id != null ? id : mongoId;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name != null ? name : "";
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email != null ? email : "";
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash != null ? passwordHash : "";
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getRole() {
        return role != null ? role : "user";
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(role);
    }

    public String getCurrency() {
        return (currency != null && !currency.trim().isEmpty()) ? currency.toUpperCase() : "INR";
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getPhone() {
        return phone != null ? phone : "";
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public double getIncomeTarget() {
        return incomeTarget;
    }

    public void setIncomeTarget(double incomeTarget) {
        this.incomeTarget = incomeTarget;
    }

    public double getSavingsTarget() {
        return savingsTarget;
    }

    public void setSavingsTarget(double savingsTarget) {
        this.savingsTarget = savingsTarget;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
