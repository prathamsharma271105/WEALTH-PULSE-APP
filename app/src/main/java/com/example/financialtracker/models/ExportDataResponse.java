package com.example.financialtracker.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class ExportDataResponse implements Serializable {
    @SerializedName("exported_at")
    private String exportedAt;

    @SerializedName("platform")
    private String platform;

    @SerializedName("version")
    private String version;

    @SerializedName("user")
    private User user;

    @SerializedName("financial_summary")
    private Map<String, Object> financialSummary;

    @SerializedName("income")
    private List<Income> income;

    @SerializedName("expenses")
    private List<Expense> expenses;

    @SerializedName("habits")
    private List<Habit> habits;

    @SerializedName("goals")
    private List<Goal> goals;

    @SerializedName("investments")
    private List<Investment> investments;

    public ExportDataResponse() {
    }

    public ExportDataResponse(User user, List<Income> income, List<Expense> expenses, List<Goal> goals, List<Habit> habits, List<Investment> investments) {
        this.user = user;
        this.income = income;
        this.expenses = expenses;
        this.goals = goals;
        this.habits = habits;
        this.investments = investments;
        this.platform = "Android";
        this.version = "1.0.0";
    }

    public String getExportedAt() {
        return exportedAt;
    }

    public String getPlatform() {
        return platform;
    }

    public String getVersion() {
        return version;
    }

    public User getUser() {
        return user;
    }

    public Map<String, Object> getFinancialSummary() {
        return financialSummary;
    }

    public List<Income> getIncome() {
        return income;
    }

    public List<Expense> getExpenses() {
        return expenses;
    }

    public List<Habit> getHabits() {
        return habits;
    }

    public List<Goal> getGoals() {
        return goals;
    }

    public List<Investment> getInvestments() {
        return investments;
    }
}
