package com.example.financialtracker.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AnalyticsSummary implements Serializable {
    @SerializedName("total_income")
    private double totalIncome;

    @SerializedName("total_expense")
    private double totalExpense;

    @SerializedName("net_cash_savings")
    private double netCashSavings;

    @SerializedName("total_invested")
    private double totalInvested;

    @SerializedName("investments_current_value")
    private double investmentsCurrentValue;

    @SerializedName("total_saved_in_goals")
    private double totalSavedInGoals;

    @SerializedName("net_worth")
    private double netWorth;

    @SerializedName("monthly")
    private List<MonthlyData> monthly;

    @SerializedName("category_breakdown")
    private List<CategoryData> categoryBreakdown;

    @SerializedName("habit_count")
    private int habitCount;

    @SerializedName("habits_completed_today")
    private int habitsCompletedToday;

    public double getTotalIncome() {
        return totalIncome;
    }

    public double getTotalExpense() {
        return totalExpense;
    }

    public double getNetCashSavings() {
        return netCashSavings;
    }

    public double getTotalInvested() {
        return totalInvested;
    }

    public double getInvestmentsCurrentValue() {
        return investmentsCurrentValue;
    }

    public double getTotalSavedInGoals() {
        return totalSavedInGoals;
    }

    public double getNetWorth() {
        return netWorth;
    }

    public List<MonthlyData> getMonthly() {
        return monthly != null ? monthly : new ArrayList<>();
    }

    public List<CategoryData> getCategoryBreakdown() {
        return categoryBreakdown != null ? categoryBreakdown : new ArrayList<>();
    }

    public int getHabitCount() {
        return habitCount;
    }

    public int getHabitsCompletedToday() {
        return habitsCompletedToday;
    }
}
