package com.example.financialtracker.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.financialtracker.R;
import com.example.financialtracker.models.AnalyticsSummary;
import com.example.financialtracker.models.CategoryData;
import com.example.financialtracker.models.User;
import com.example.financialtracker.network.ApiClient;
import com.example.financialtracker.utils.FormatUtils;
import com.example.financialtracker.utils.SessionManager;
import com.example.financialtracker.utils.ThemeManager;
import com.example.financialtracker.views.BarChartView;
import com.example.financialtracker.views.DonutChartView;
import java.text.DecimalFormat;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AnalyticsActivity extends AppCompatActivity {

    private SwipeRefreshLayout swipeRefresh;
    private ImageView btnBack;
    private TextView tvSavingsRate;
    private TextView tvNetCash;
    private BarChartView barChart;
    private DonutChartView donutChart;
    private LinearLayout llCategoriesList;

    private String currency = "INR";
    private String currentAppliedTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        currentAppliedTheme = SessionManager.getInstance(this).getTheme();
        setContentView(R.layout.activity_analytics);

        swipeRefresh = findViewById(R.id.swipe_refresh_analytics);
        btnBack = findViewById(R.id.btn_back_analytics);
        tvSavingsRate = findViewById(R.id.tv_analytics_savings_rate);
        tvNetCash = findViewById(R.id.tv_analytics_net_cash);
        barChart = findViewById(R.id.chart_analytics_bar);
        donutChart = findViewById(R.id.chart_analytics_donut);
        llCategoriesList = findViewById(R.id.ll_categories_list);

        User user = SessionManager.getInstance(this).getUser();
        if (user != null) {
            currency = user.getCurrency();
        }

        btnBack.setOnClickListener(v -> finish());
        swipeRefresh.setOnRefreshListener(this::loadAnalytics);

        loadAnalytics();
    }

    @Override
    protected void onResume() {
        super.onResume();
        String activeTheme = SessionManager.getInstance(this).getTheme();
        if (currentAppliedTheme != null && !currentAppliedTheme.equalsIgnoreCase(activeTheme)) {
            currentAppliedTheme = activeTheme;
            Intent intent = getIntent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            finish();
            overridePendingTransition(0, 0);
            startActivity(intent);
            overridePendingTransition(0, 0);
            return;
        }
    }

    private void loadAnalytics() {
        swipeRefresh.setRefreshing(true);
        ApiClient.getService(this).getAnalyticsSummary().enqueue(new Callback<AnalyticsSummary>() {
            @Override
            public void onResponse(Call<AnalyticsSummary> call, Response<AnalyticsSummary> response) {
                swipeRefresh.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    bindData(response.body());
                }
            }

            @Override
            public void onFailure(Call<AnalyticsSummary> call, Throwable t) {
                swipeRefresh.setRefreshing(false);
                Toast.makeText(AnalyticsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindData(AnalyticsSummary s) {
        double inc = s.getTotalIncome();
        double exp = s.getTotalExpense();
        double savings = inc - exp;
        double rate = inc > 0 ? (savings / inc) * 100.0 : 0.0;
        if (rate < 0) rate = 0;

        DecimalFormat df = new DecimalFormat("#,##0.0");
        tvSavingsRate.setText(df.format(rate) + "%");
        tvNetCash.setText(FormatUtils.formatCurrency(savings, currency));

        boolean isDark = !SessionManager.THEME_LIGHT.equalsIgnoreCase(SessionManager.getInstance(this).getTheme());
        barChart.setData(s.getMonthly(), isDark);
        donutChart.setData(s.getCategoryBreakdown(), FormatUtils.formatCurrency(exp, currency), isDark);

        renderCategoryBreakdown(s.getCategoryBreakdown(), exp);
    }

    private void renderCategoryBreakdown(List<CategoryData> list, double totalExpense) {
        llCategoriesList.removeAllViews();
        if (list == null || list.isEmpty() || totalExpense <= 0) {
            TextView empty = new TextView(this);
            empty.setText("No expense data recorded.");
            empty.setTextColor(Color.parseColor("#9CA3AF"));
            empty.setPadding(0, 16, 0, 16);
            llCategoriesList.addView(empty);
            return;
        }

        for (CategoryData cat : list) {
            View row = LayoutInflater.from(this).inflate(R.layout.item_category_stat, llCategoriesList, false);
            TextView tvCatName = row.findViewById(R.id.tv_cat_stat_name);
            TextView tvCatAmt = row.findViewById(R.id.tv_cat_stat_amount);
            TextView tvCatPct = row.findViewById(R.id.tv_cat_stat_percent);
            ProgressBar pb = row.findViewById(R.id.pb_cat_stat);

            int pct = (int) Math.round((cat.getTotal() / totalExpense) * 100);
            tvCatName.setText(cat.getCategory());
            tvCatAmt.setText(FormatUtils.formatCurrency(cat.getTotal(), currency));
            tvCatPct.setText(pct + "%");
            pb.setProgress(pct);

            llCategoriesList.addView(row);
        }
    }
}
