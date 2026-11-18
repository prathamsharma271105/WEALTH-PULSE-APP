package com.example.financialtracker.activities;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.financialtracker.R;
import com.example.financialtracker.adapters.InvestmentAdapter;
import com.example.financialtracker.models.ApiResponse;
import com.example.financialtracker.models.Investment;
import com.example.financialtracker.models.InvestmentRequest;
import com.example.financialtracker.models.InvestmentUpdateRequest;
import com.example.financialtracker.models.User;
import com.example.financialtracker.network.ApiClient;
import com.example.financialtracker.network.ApiService;
import com.example.financialtracker.utils.FormatUtils;
import com.example.financialtracker.utils.SessionManager;
import com.example.financialtracker.utils.ThemeManager;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InvestmentsActivity extends AppCompatActivity {

    private SwipeRefreshLayout swipeRefresh;
    private ImageView btnBack;
    private Button btnAddAsset;
    private TextView tvPortfolioValue;
    private TextView tvInvestedCapital;
    private TextView tvUnrealizedPl;
    private RecyclerView rvInvestments;
    private TextView tvNoInvestments;

    private InvestmentAdapter adapter;
    private String currency = "INR";

    private final String[] assetTypes = new String[]{
            "Mutual Funds",
            "Direct Stocks",
            "Index ETF",
            "Gold",
            "Fixed Deposit",
            "PPF / EPF",
            "Real Estate",
            "Crypto",
            "Other"
    };
    private String currentAppliedTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        currentAppliedTheme = SessionManager.getInstance(this).getTheme();
        setContentView(R.layout.activity_investments);

        swipeRefresh = findViewById(R.id.swipe_refresh_investments);
        btnBack = findViewById(R.id.btn_back_investments);
        btnAddAsset = findViewById(R.id.btn_add_asset);
        tvPortfolioValue = findViewById(R.id.tv_total_portfolio_value);
        tvInvestedCapital = findViewById(R.id.tv_total_invested_capital);
        tvUnrealizedPl = findViewById(R.id.tv_total_unrealized_pl);
        rvInvestments = findViewById(R.id.rv_investments);
        tvNoInvestments = findViewById(R.id.tv_no_investments);

        User user = SessionManager.getInstance(this).getUser();
        if (user != null) {
            currency = user.getCurrency();
        }

        btnBack.setOnClickListener(v -> finish());
        btnAddAsset.setOnClickListener(v -> showAddAssetDialog());
        swipeRefresh.setOnRefreshListener(this::loadInvestments);

        setupRecyclerView();
        loadInvestments();
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

    private void setupRecyclerView() {
        rvInvestments.setLayoutManager(new LinearLayoutManager(this));
        adapter = new InvestmentAdapter(this, currency, new InvestmentAdapter.InvestmentActionListener() {
            @Override
            public void onEditValue(Investment investment) {
                showEditValueDialog(investment);
            }

            @Override
            public void onDelete(Investment investment) {
                deleteInvestment(investment);
            }
        });
        rvInvestments.setAdapter(adapter);
    }

    public void loadInvestments() {
        swipeRefresh.setRefreshing(true);
        ApiClient.getService(this).getInvestments().enqueue(new Callback<List<Investment>>() {
            @Override
            public void onResponse(Call<List<Investment>> call, Response<List<Investment>> response) {
                swipeRefresh.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<Investment> list = response.body();
                    adapter.setInvestments(list);
                    tvNoInvestments.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                    updateSummary(list);
                }
            }

            @Override
            public void onFailure(Call<List<Investment>> call, Throwable t) {
                swipeRefresh.setRefreshing(false);
                Toast.makeText(InvestmentsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateSummary(List<Investment> list) {
        double totalInvested = 0;
        double totalCurrent = 0;

        for (Investment item : list) {
            totalInvested += item.getAmountInvested();
            totalCurrent += item.getCurrentValue();
        }

        double pl = totalCurrent - totalInvested;
        double plPct = totalInvested > 0 ? (pl / totalInvested) * 100.0 : 0;
        DecimalFormat pctFmt = new DecimalFormat("#,##0.0");

        tvPortfolioValue.setText(FormatUtils.formatCurrency(totalCurrent, currency));
        tvInvestedCapital.setText(FormatUtils.formatCurrency(totalInvested, currency));

        if (pl >= 0) {
            tvUnrealizedPl.setText("+" + FormatUtils.formatCurrency(pl, currency) + " (+" + pctFmt.format(plPct) + "%)");
            tvUnrealizedPl.setTextColor(Color.parseColor("#10B981"));
        } else {
            tvUnrealizedPl.setText(FormatUtils.formatCurrency(pl, currency) + " (" + pctFmt.format(plPct) + "%)");
            tvUnrealizedPl.setTextColor(Color.parseColor("#F43F5E"));
        }
    }

    private void showAddAssetDialog() {
        View dView = LayoutInflater.from(this).inflate(R.layout.dialog_add_investment, null);
        EditText etName = dView.findViewById(R.id.et_asset_name);
        Spinner spType = dView.findViewById(R.id.sp_asset_type);
        EditText etInvested = dView.findViewById(R.id.et_amount_invested);
        EditText etCurrent = dView.findViewById(R.id.et_current_value);
        EditText etDate = dView.findViewById(R.id.et_investment_date);

        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, assetTypes);
        spType.setAdapter(typeAdapter);

        etDate.setText(FormatUtils.todayDateString());
        etDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                String m = (month + 1 < 10) ? "0" + (month + 1) : String.valueOf(month + 1);
                String d = (dayOfMonth < 10) ? "0" + dayOfMonth : String.valueOf(dayOfMonth);
                etDate.setText(year + "-" + m + "-" + d);
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        new AlertDialog.Builder(this)
                .setTitle("+ Add Asset to Portfolio")
                .setView(dView)
                .setPositiveButton("Save Asset", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String type = assetTypes[spType.getSelectedItemPosition()];
                    String invStr = etInvested.getText().toString().trim();
                    String curStr = etCurrent.getText().toString().trim();
                    String date = etDate.getText().toString().trim();

                    if (name.isEmpty() || invStr.isEmpty() || curStr.isEmpty()) {
                        Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double invested = Double.parseDouble(invStr);
                    double current = Double.parseDouble(curStr);

                    InvestmentRequest req = new InvestmentRequest(name, type, invested, current, date);
                    ApiClient.getService(this).addInvestment(req).enqueue(new Callback<Investment>() {
                        @Override
                        public void onResponse(Call<Investment> call, Response<Investment> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(InvestmentsActivity.this, "Asset added!", Toast.LENGTH_SHORT).show();
                                loadInvestments();
                            } else {
                                Toast.makeText(InvestmentsActivity.this, "Failed to add asset: " + response.code(), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Investment> call, Throwable t) {
                            Toast.makeText(InvestmentsActivity.this, "Failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditValueDialog(Investment item) {
        View dView = LayoutInflater.from(this).inflate(R.layout.dialog_update_investment_value, null);
        EditText etVal = dView.findViewById(R.id.et_updated_current_value);
        etVal.setText(String.valueOf(item.getCurrentValue()));

        new AlertDialog.Builder(this)
                .setTitle("Update Current Value: " + item.getAssetName())
                .setView(dView)
                .setPositiveButton("Update", (dialog, which) -> {
                    String valStr = etVal.getText().toString().trim();
                    if (valStr.isEmpty()) return;
                    double val = Double.parseDouble(valStr);

                    ApiClient.getService(this).updateInvestmentValue(item.getId(), new InvestmentUpdateRequest(val))
                            .enqueue(new Callback<Investment>() {
                                @Override
                                public void onResponse(Call<Investment> call, Response<Investment> response) {
                                    if (response.isSuccessful()) {
                                        Toast.makeText(InvestmentsActivity.this, "Valuation updated!", Toast.LENGTH_SHORT).show();
                                        loadInvestments();
                                    }
                                }

                                @Override
                                public void onFailure(Call<Investment> call, Throwable t) {}
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteInvestment(Investment item) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Asset")
                .setMessage("Remove '" + item.getAssetName() + "' from your portfolio?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    ApiClient.getService(this).deleteInvestment(item.getId()).enqueue(new Callback<ApiResponse>() {
                        @Override
                        public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                            Toast.makeText(InvestmentsActivity.this, "Asset deleted", Toast.LENGTH_SHORT).show();
                            loadInvestments();
                        }

                        @Override
                        public void onFailure(Call<ApiResponse> call, Throwable t) {}
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
