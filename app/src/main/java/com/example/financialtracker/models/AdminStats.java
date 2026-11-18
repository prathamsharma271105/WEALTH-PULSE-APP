package com.example.financialtracker.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class AdminStats implements Serializable {
    @SerializedName("total_users")
    private int totalUsers;

    @SerializedName("total_income")
    private double totalIncome;

    @SerializedName("total_expense")
    private double totalExpense;

    @SerializedName("total_goals")
    private int totalGoals;

    @SerializedName("total_habits")
    private int totalHabits;

    @SerializedName("total_habit_completions")
    private int totalHabitCompletions;

    @SerializedName("habits_completed_today")
    private int habitsCompletedToday;

    @SerializedName("open_feedback")
    private int openFeedback;

    public int getTotalUsers() {
        return totalUsers;
    }

    public double getTotalIncome() {
        return totalIncome;
    }

    public double getTotalExpense() {
        return totalExpense;
    }

    public int getTotalGoals() {
        return totalGoals;
    }

    public int getTotalHabits() {
        return totalHabits;
    }

    public int getTotalHabitCompletions() {
        return totalHabitCompletions;
    }

    public int getHabitsCompletedToday() {
        return habitsCompletedToday;
    }

    public int getOpenFeedback() {
        return openFeedback;
    }
}
