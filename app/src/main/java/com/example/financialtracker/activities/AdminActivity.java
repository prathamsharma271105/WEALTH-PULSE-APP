package com.example.financialtracker.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.financialtracker.R;
import com.example.financialtracker.adapters.AdminFeedbackAdapter;
import com.example.financialtracker.adapters.AdminUserAdapter;
import com.example.financialtracker.models.*;
import com.example.financialtracker.network.ApiClient;
import com.example.financialtracker.network.ApiService;
import com.example.financialtracker.utils.FormatUtils;
import com.example.financialtracker.utils.SessionManager;
import com.example.financialtracker.utils.ThemeManager;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminActivity extends AppCompatActivity {

    private SwipeRefreshLayout swipeRefresh;
    private ImageView btnBack;
    private TextView tvTotalUsers;
    private TextView tvOpenFeedback;
    private TextView tvSystemIncome;
    private TextView tvSystemExpense;

    private Button btnTabUsers;
    private Button btnTabFeedback;
    private RecyclerView rvUsers;
    private RecyclerView rvFeedback;
    private TextView tvEmptyMsg;

    private AdminUserAdapter userAdapter;
    private AdminFeedbackAdapter feedbackAdapter;
    private int currentTab = 0; // 0 = Users, 1 = Feedback
    private String currentAppliedTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        currentAppliedTheme = SessionManager.getInstance(this).getTheme();

        User currentUser = SessionManager.getInstance(this).getUser();
        if (currentUser == null || !currentUser.isAdmin()) {
            Toast.makeText(this, "Access restricted to administrators only", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_admin);

        swipeRefresh = findViewById(R.id.swipe_refresh_admin);
        btnBack = findViewById(R.id.btn_back_admin);
        tvTotalUsers = findViewById(R.id.tv_admin_total_users);
        tvOpenFeedback = findViewById(R.id.tv_admin_open_feedback);
        tvSystemIncome = findViewById(R.id.tv_admin_system_income);
        tvSystemExpense = findViewById(R.id.tv_admin_system_expense);

        btnTabUsers = findViewById(R.id.btn_admin_tab_users);
        btnTabFeedback = findViewById(R.id.btn_admin_tab_feedback);
        rvUsers = findViewById(R.id.rv_admin_users);
        rvFeedback = findViewById(R.id.rv_admin_feedback);
        tvEmptyMsg = findViewById(R.id.tv_admin_empty_msg);

        btnBack.setOnClickListener(v -> finish());
        swipeRefresh.setOnRefreshListener(this::loadAdminData);

        btnTabUsers.setOnClickListener(v -> switchTab(0));
        btnTabFeedback.setOnClickListener(v -> switchTab(1));

        setupRecyclerViews(currentUser.getId());
        loadAdminData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        String activeTheme = SessionManager.getInstance(this).getTheme();
        if (currentAppliedTheme != null && !currentAppliedTheme.equalsIgnoreCase(activeTheme)) {
            currentAppliedTheme = activeTheme;
            android.content.Intent intent = getIntent();
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NO_ANIMATION);
            finish();
            overridePendingTransition(0, 0);
            startActivity(intent);
            overridePendingTransition(0, 0);
            return;
        }
    }

    private void setupRecyclerViews(String currentUserId) {
        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        userAdapter = new AdminUserAdapter(this, currentUserId, new AdminUserAdapter.AdminUserActionListener() {
            @Override
            public void onToggleRole(AdminUserItem user) {
                toggleUserRole(user);
            }

            @Override
            public void onDelete(AdminUserItem user) {
                deleteUser(user);
            }
        });
        rvUsers.setAdapter(userAdapter);

        rvFeedback.setLayoutManager(new LinearLayoutManager(this));
        feedbackAdapter = new AdminFeedbackAdapter(this, item -> resolveFeedback(item));
        rvFeedback.setAdapter(feedbackAdapter);
    }

    private void switchTab(int tab) {
        currentTab = tab;
        btnTabUsers.setBackgroundResource(tab == 0 ? R.drawable.btn_primary : R.drawable.btn_ghost);
        btnTabUsers.setTextColor(tab == 0 ? androidx.core.content.ContextCompat.getColor(this, R.color.white) : androidx.core.content.ContextCompat.getColor(this, R.color.slate_text_muted));

        btnTabFeedback.setBackgroundResource(tab == 1 ? R.drawable.btn_primary : R.drawable.btn_ghost);
        btnTabFeedback.setTextColor(tab == 1 ? androidx.core.content.ContextCompat.getColor(this, R.color.white) : androidx.core.content.ContextCompat.getColor(this, R.color.slate_text_muted));

        rvUsers.setVisibility(tab == 0 ? View.VISIBLE : View.GONE);
        rvFeedback.setVisibility(tab == 1 ? View.VISIBLE : View.GONE);

        loadAdminData();
    }

    public void loadAdminData() {
        swipeRefresh.setRefreshing(true);
        ApiService api = ApiClient.getService(this);

        // 1. Stats
        api.getAdminStats().enqueue(new Callback<AdminStats>() {
            @Override
            public void onResponse(Call<AdminStats> call, Response<AdminStats> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AdminStats stats = response.body();
                    tvTotalUsers.setText(String.valueOf(stats.getTotalUsers()));
                    tvOpenFeedback.setText(String.valueOf(stats.getOpenFeedback()));
                    tvSystemIncome.setText("+" + FormatUtils.formatCurrency(stats.getTotalIncome(), "INR"));
                    tvSystemExpense.setText("-" + FormatUtils.formatCurrency(stats.getTotalExpense(), "INR"));
                }
            }

            @Override
            public void onFailure(Call<AdminStats> call, Throwable t) {}
        });

        if (currentTab == 0) {
            // Load users
            api.getAdminUsers().enqueue(new Callback<List<AdminUserItem>>() {
                @Override
                public void onResponse(Call<List<AdminUserItem>> call, Response<List<AdminUserItem>> response) {
                    swipeRefresh.setRefreshing(false);
                    if (response.isSuccessful() && response.body() != null) {
                        List<AdminUserItem> users = response.body();
                        userAdapter.setUsers(users);
                        tvEmptyMsg.setVisibility(users.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                }

                @Override
                public void onFailure(Call<List<AdminUserItem>> call, Throwable t) {
                    swipeRefresh.setRefreshing(false);
                }
            });
        } else {
            // Load feedback
            api.getAdminFeedback().enqueue(new Callback<List<FeedbackItem>>() {
                @Override
                public void onResponse(Call<List<FeedbackItem>> call, Response<List<FeedbackItem>> response) {
                    swipeRefresh.setRefreshing(false);
                    if (response.isSuccessful() && response.body() != null) {
                        List<FeedbackItem> list = response.body();
                        feedbackAdapter.setFeedbacks(list);
                        tvEmptyMsg.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                }

                @Override
                public void onFailure(Call<List<FeedbackItem>> call, Throwable t) {
                    swipeRefresh.setRefreshing(false);
                }
            });
        }
    }

    private void toggleUserRole(AdminUserItem user) {
        String newRole = user.isAdmin() ? "user" : "admin";
        new AlertDialog.Builder(this)
                .setTitle("Change User Role")
                .setMessage("Change role of " + user.getName() + " to '" + newRole.toUpperCase() + "'?")
                .setPositiveButton("Confirm", (dialog, which) -> {
                    ApiClient.getService(this).updateUserRole(user.getId(), new RoleUpdateRequest(newRole))
                            .enqueue(new Callback<ApiResponse>() {
                                @Override
                                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                                    if (response.isSuccessful()) {
                                        Toast.makeText(AdminActivity.this, "Role updated", Toast.LENGTH_SHORT).show();
                                        loadAdminData();
                                    }
                                }

                                @Override
                                public void onFailure(Call<ApiResponse> call, Throwable t) {}
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteUser(AdminUserItem user) {
        new AlertDialog.Builder(this)
                .setTitle("Delete User Account")
                .setMessage("Permanently remove user '" + user.getName() + "' and all their data?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    ApiClient.getService(this).deleteAdminUser(user.getId()).enqueue(new Callback<ApiResponse>() {
                        @Override
                        public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(AdminActivity.this, "User deleted", Toast.LENGTH_SHORT).show();
                                loadAdminData();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse> call, Throwable t) {}
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void resolveFeedback(FeedbackItem item) {
        ApiClient.getService(this).updateFeedbackStatus(item.getId(), new FeedbackStatusRequest("resolved"))
                .enqueue(new Callback<ApiResponse>() {
                    @Override
                    public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(AdminActivity.this, "Feedback resolved!", Toast.LENGTH_SHORT).show();
                            loadAdminData();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse> call, Throwable t) {}
                });
    }
}
