package com.example.financialtracker.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class HabitLog implements Serializable {
    @SerializedName("id")
    private Integer id;

    @SerializedName("habit_id")
    private Integer habitId;

    @SerializedName("user_id")
    private Integer userId;

    @SerializedName("completed_on")
    private String completedOn;

    public HabitLog() {}

    public HabitLog(Integer habitId, Integer userId, String completedOn) {
        this.habitId = habitId;
        this.userId = userId;
        this.completedOn = completedOn;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getHabitId() {
        return habitId;
    }

    public void setHabitId(Integer habitId) {
        this.habitId = habitId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getCompletedOn() {
        return completedOn;
    }

    public void setCompletedOn(String completedOn) {
        this.completedOn = completedOn;
    }
}
