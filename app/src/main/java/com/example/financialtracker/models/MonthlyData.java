package com.example.financialtracker.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class MonthlyData implements Serializable {
    @SerializedName("month")
    private String month;

    @SerializedName("income")
    private double income;

    @SerializedName("expense")
    private double expense;

    public MonthlyData() {}

    public MonthlyData(String month, double income, double expense) {
        this.month = month;
        this.income = income;
        this.expense = expense;
    }

    public String getMonth() {
        return month != null ? month : "";
    }

    public double getIncome() {
        return income;
    }

    public double getExpense() {
        return expense;
    }

    public double getSavings() {
        return income - expense;
    }
}
