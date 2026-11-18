package com.example.financialtracker.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class CategoryData implements Serializable {
    @SerializedName("category")
    private String category;

    @SerializedName("total")
    private double total;

    public CategoryData() {}

    public CategoryData(String category, double total, double percentage) {
        this.category = category;
        this.total = total;
    }

    public String getCategory() {
        return category != null ? category : "Other";
    }

    public double getTotal() {
        return total;
    }
}
