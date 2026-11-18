package com.example.financialtracker.models;

import com.google.gson.annotations.SerializedName;

public class HabitToggleResponse {
    @SerializedName("ok")
    private boolean ok;

    @SerializedName("completed_today")
    private boolean completedToday;

    @SerializedName("streak")
    private int streak;

    @SerializedName("total_completions")
    private int totalCompletions;

    public HabitToggleResponse() {
    }

    public HabitToggleResponse(boolean completedToday, int streak, int totalCompletions) {
        this.ok = true;
        this.completedToday = completedToday;
        this.streak = streak;
        this.totalCompletions = totalCompletions;
    }

    public boolean isOk() {
        return ok;
    }

    public boolean isCompletedToday() {
        return completedToday;
    }

    public int getStreak() {
        return streak;
    }

    public int getTotalCompletions() {
        return totalCompletions;
    }
}
