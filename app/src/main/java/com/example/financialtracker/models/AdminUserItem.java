package com.example.financialtracker.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class AdminUserItem implements Serializable {
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

    @SerializedName("currency")
    private String currency;

    @SerializedName("phone")
    private String phone;

    @SerializedName("income_target")
    private double incomeTarget;

    @SerializedName("savings_target")
    private double savingsTarget;

    @SerializedName("income_count")
    private int incomeCount;

    @SerializedName("expense_count")
    private int expenseCount;

    @SerializedName("habit_count")
    private int habitCount;

    @SerializedName("goal_count")
    private int goalCount;

    @SerializedName("created_at")
    private String createdAt;

    public AdminUserItem() {}

    public AdminUserItem(String id, String name, String email, String role, String createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id != null ? id : mongoId;
    }

    public String getName() {
        return name != null ? name : "";
    }

    public String getEmail() {
        return email != null ? email : "";
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
        return currency != null ? currency : "INR";
    }

    public String getPhone() {
        return phone != null ? phone : "";
    }

    public int getIncomeCount() {
        return incomeCount;
    }

    public int getExpenseCount() {
        return expenseCount;
    }

    public int getHabitCount() {
        return habitCount;
    }

    public int getGoalCount() {
        return goalCount;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
