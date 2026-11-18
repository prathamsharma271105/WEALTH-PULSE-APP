package com.example.financialtracker.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.financialtracker.R;
import com.example.financialtracker.adapters.GoalAdapter;
import com.example.financialtracker.adapters.HabitAdapter;
import com.example.financialtracker.models.*;
import com.example.financialtracker.network.ApiClient;
import com.example.financialtracker.network.ApiService;
import com.example.financialtracker.utils.FormatUtils;
import com.example.financialtracker.utils.SessionManager;
import com.example.financialtracker.views.BarChartView;
import com.example.financialtracker.views.DonutChartView;
import java.util.Calendar;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardFragment extends Fragment {

    private SwipeRefreshLayout swipeRefresh;
    private TextView tvNetWorth;
    private TextView tvLiquidSavings;
    private TextView tvInvestmentsVal;
    private TextView tvIncome;
    private TextView tvExpense;
    private Button btnAddIncome;
    private Button btnAddExpense;
    private BarChartView barChart;
    private DonutChartView donutChart;
    private TextView tvHabitsCompleted;
    private RecyclerView rvHabits;
    private TextView tvNoHabits;
    private TextView tvGoalsTotalSaved;
    private RecyclerView rvGoals;
    private TextView tvNoGoals;

    private HabitAdapter habitAdapter;
    private GoalAdapter goalAdapter;
    private String currency = "INR";

    private final String[] expenseCategories = new String[]{
            "Food & Dining",
            "Transport & Fuel",
            "Rent & Housing",
            "Utilities & Bills",
            "Entertainment & Leisure",
            "Shopping & Essentials",
            "Health & Medical",
            "Education & Courses",
            "Other"
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        swipeRefresh = view.findViewById(R.id.swipe_refresh_dashboard);
        tvNetWorth = view.findViewById(R.id.tv_dash_net_worth);
        tvLiquidSavings = view.findViewById(R.id.tv_dash_liquid_savings);
        tvInvestmentsVal = view.findViewById(R.id.tv_dash_investments_val);
        tvIncome = view.findViewById(R.id.tv_dash_income);
        tvExpense = view.findViewById(R.id.tv_dash_expense);
        btnAddIncome = view.findViewById(R.id.btn_dash_add_income);
        btnAddExpense = view.findViewById(R.id.btn_dash_add_expense);
        barChart = view.findViewById(R.id.chart_dash_bar);
        donutChart = view.findViewById(R.id.chart_dash_donut);
        tvHabitsCompleted = view.findViewById(R.id.tv_dash_habits_completed);
        rvHabits = view.findViewById(R.id.rv_dash_habits);
        tvNoHabits = view.findViewById(R.id.tv_dash_no_habits);
        tvGoalsTotalSaved = view.findViewById(R.id.tv_dash_goals_total_saved);
        rvGoals = view.findViewById(R.id.rv_dash_goals);
        tvNoGoals = view.findViewById(R.id.tv_dash_no_goals);

        User user = SessionManager.getInstance(requireContext()).getUser();
        if (user != null) {
            currency = user.getCurrency();
        }

        setupRecyclerViews();

        swipeRefresh.setOnRefreshListener(this::loadDashboardData);
        btnAddIncome.setOnClickListener(v -> showAddIncomeDialog());
        btnAddExpense.setOnClickListener(v -> showAddExpenseDialog());

        loadDashboardData();

        return view;
    }

    private void setupRecyclerViews() {
        rvHabits.setLayoutManager(new LinearLayoutManager(requireContext()));
        habitAdapter = new HabitAdapter(requireContext(), currency, new HabitAdapter.HabitActionListener() {
            @Override
            public void onToggle(Habit habit, boolean isChecked) {
                toggleHabitCompletion(habit);
            }

            @Override
            public void onDelete(Habit habit) {
                deleteHabit(habit);
            }
        });
        rvHabits.setAdapter(habitAdapter);

        rvGoals.setLayoutManager(new LinearLayoutManager(requireContext()));
        goalAdapter = new GoalAdapter(requireContext(), currency, new GoalAdapter.GoalActionListener() {
            @Override
            public void onContribute(Goal goal) {
                showContributeDialog(goal);
            }

            @Override
            public void onDelete(Goal goal) {
                deleteGoal(goal);
            }
        });
        rvGoals.setAdapter(goalAdapter);
    }

    public void loadDashboardData() {
        swipeRefresh.setRefreshing(true);
        ApiService api = ApiClient.getService(requireContext());

        // 1. Analytics Summary
        api.getAnalyticsSummary().enqueue(new Callback<AnalyticsSummary>() {
            @Override
            public void onResponse(Call<AnalyticsSummary> call, Response<AnalyticsSummary> response) {
                if (isAdded() && response.isSuccessful() && response.body() != null) {
                    bindAnalytics(response.body());
                }
            }

            @Override
            public void onFailure(Call<AnalyticsSummary> call, Throwable t) {}
        });

        // 2. Habits
        api.getHabits().enqueue(new Callback<List<Habit>>() {
            @Override
            public void onResponse(Call<List<Habit>> call, Response<List<Habit>> response) {
                if (isAdded() && response.isSuccessful() && response.body() != null) {
                    List<Habit> list = response.body();
                    habitAdapter.setHabits(list.size() > 3 ? list.subList(0, 3) : list);
                    tvNoHabits.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                }
            }

            @Override
            public void onFailure(Call<List<Habit>> call, Throwable t) {}
        });

        // 3. Goals
        api.getGoals().enqueue(new Callback<List<Goal>>() {
            @Override
            public void onResponse(Call<List<Goal>> call, Response<List<Goal>> response) {
                if (isAdded()) {
                    swipeRefresh.setRefreshing(false);
                    if (response.isSuccessful() && response.body() != null) {
                        List<Goal> list = response.body();
                        goalAdapter.setGoals(list.size() > 3 ? list.subList(0, 3) : list);
                        tvNoGoals.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);

                        double savedTotal = 0;
                        for (Goal g : list) savedTotal += g.getSavedAmount();
                        tvGoalsTotalSaved.setText("Saved: " + FormatUtils.formatCurrency(savedTotal, currency));
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Goal>> call, Throwable t) {
                if (isAdded()) swipeRefresh.setRefreshing(false);
            }
        });
    }

    private void bindAnalytics(AnalyticsSummary s) {
        tvNetWorth.setText(FormatUtils.formatCurrency(s.getNetWorth(), currency));
        tvLiquidSavings.setText(FormatUtils.formatCurrency(s.getNetCashSavings(), currency));
        tvInvestmentsVal.setText(FormatUtils.formatCurrency(s.getInvestmentsCurrentValue(), currency));
        tvIncome.setText("+" + FormatUtils.formatCurrency(s.getTotalIncome(), currency));
        tvExpense.setText("-" + FormatUtils.formatCurrency(s.getTotalExpense(), currency));

        tvHabitsCompleted.setText(s.getHabitsCompletedToday() + "/" + s.getHabitCount() + " Completed Today");

        boolean isDark = !SessionManager.THEME_LIGHT.equalsIgnoreCase(SessionManager.getInstance(requireContext()).getTheme());
        barChart.setData(s.getMonthly(), isDark);
        donutChart.setData(s.getCategoryBreakdown(), FormatUtils.formatCurrency(s.getTotalExpense(), currency), isDark);
    }

    private void toggleHabitCompletion(Habit habit) {
        ApiService api = ApiClient.getService(requireContext());
        api.toggleHabit(habit.getId()).enqueue(new Callback<HabitToggleResponse>() {
            @Override
            public void onResponse(Call<HabitToggleResponse> call, Response<HabitToggleResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    HabitToggleResponse res = response.body();
                    habit.setCompletedToday(res.isCompletedToday());
                    habit.setStreak(res.getStreak());
                    habit.setTotalCompletions(res.getTotalCompletions());
                    habitAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<HabitToggleResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Sync error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteHabit(Habit habit) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Habit")
                .setMessage("Delete habit '" + habit.getName() + "' and its streak history?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    ApiClient.getService(requireContext()).deleteHabit(habit.getId()).enqueue(new Callback<ApiResponse>() {
                        @Override
                        public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                            Toast.makeText(getContext(), "Habit removed", Toast.LENGTH_SHORT).show();
                            loadDashboardData();
                        }

                        @Override
                        public void onFailure(Call<ApiResponse> call, Throwable t) {}
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showContributeDialog(Goal goal) {
        View dView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_contribution, null);
        EditText etAmount = dView.findViewById(R.id.et_contrib_amount);

        new AlertDialog.Builder(requireContext())
                .setTitle("Contribute to " + goal.getTitle())
                .setView(dView)
                .setPositiveButton("Contribute", (dialog, which) -> {
                    String amtStr = etAmount.getText().toString().trim();
                    if (amtStr.isEmpty()) return;
                    double amt = Double.parseDouble(amtStr);
                    if (amt <= 0) return;

                    ApiClient.getService(requireContext()).contributeGoal(goal.getId(), new GoalContributeRequest(amt))
                            .enqueue(new Callback<Goal>() {
                                @Override
                                public void onResponse(Call<Goal> call, Response<Goal> response) {
                                    if (response.isSuccessful()) {
                                        Toast.makeText(getContext(), "Contribution recorded!", Toast.LENGTH_SHORT).show();
                                        loadDashboardData();
                                    }
                                }

                                @Override
                                public void onFailure(Call<Goal> call, Throwable t) {}
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteGoal(Goal goal) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Goal")
                .setMessage("Delete savings goal '" + goal.getTitle() + "'?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    ApiClient.getService(requireContext()).deleteGoal(goal.getId()).enqueue(new Callback<ApiResponse>() {
                        @Override
                        public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                            Toast.makeText(getContext(), "Goal deleted", Toast.LENGTH_SHORT).show();
                            loadDashboardData();
                        }

                        @Override
                        public void onFailure(Call<ApiResponse> call, Throwable t) {}
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showAddIncomeDialog() {
        View dView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_income, null);
        EditText etSource = dView.findViewById(R.id.et_income_source);
        EditText etAmount = dView.findViewById(R.id.et_income_amount);
        EditText etDate = dView.findViewById(R.id.et_income_date);
        EditText etNote = dView.findViewById(R.id.et_income_note);

        etDate.setText(FormatUtils.todayDateString());
        etDate.setOnClickListener(v -> showDatePicker(etDate));

        new AlertDialog.Builder(requireContext())
                .setTitle("+ Add Income Inflow")
                .setView(dView)
                .setPositiveButton("Save Income", (dialog, which) -> {
                    String source = etSource.getText().toString().trim();
                    String amtStr = etAmount.getText().toString().trim();
                    String date = etDate.getText().toString().trim();
                    String note = etNote.getText().toString().trim();

                    if (source.isEmpty() || amtStr.isEmpty()) {
                        Toast.makeText(getContext(), "Source and Amount required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double amount = Double.parseDouble(amtStr);
                    ApiClient.getService(requireContext()).addIncome(new IncomeRequest(source, amount, date, note))
                            .enqueue(new Callback<Income>() {
                                @Override
                                public void onResponse(Call<Income> call, Response<Income> response) {
                                    if (response.isSuccessful()) {
                                        Toast.makeText(getContext(), "Income saved!", Toast.LENGTH_SHORT).show();
                                        loadDashboardData();
                                    }
                                }

                                @Override
                                public void onFailure(Call<Income> call, Throwable t) {}
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showAddExpenseDialog() {
        View dView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_expense, null);
        Spinner spCategory = dView.findViewById(R.id.sp_expense_category);
        EditText etAmount = dView.findViewById(R.id.et_expense_amount);
        EditText etDate = dView.findViewById(R.id.et_expense_date);
        EditText etNote = dView.findViewById(R.id.et_expense_note);

        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, expenseCategories);
        spCategory.setAdapter(catAdapter);

        etDate.setText(FormatUtils.todayDateString());
        etDate.setOnClickListener(v -> showDatePicker(etDate));

        new AlertDialog.Builder(requireContext())
                .setTitle("- Record Expense Outflow")
                .setView(dView)
                .setPositiveButton("Save Expense", (dialog, which) -> {
                    String category = expenseCategories[spCategory.getSelectedItemPosition()];
                    String amtStr = etAmount.getText().toString().trim();
                    String date = etDate.getText().toString().trim();
                    String note = etNote.getText().toString().trim();

                    if (amtStr.isEmpty()) {
                        Toast.makeText(getContext(), "Amount is required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double amount = Double.parseDouble(amtStr);
                    ApiClient.getService(requireContext()).addExpense(new ExpenseRequest(category, amount, date, note))
                            .enqueue(new Callback<Expense>() {
                                @Override
                                public void onResponse(Call<Expense> call, Response<Expense> response) {
                                    if (response.isSuccessful()) {
                                        Toast.makeText(getContext(), "Expense recorded!", Toast.LENGTH_SHORT).show();
                                        loadDashboardData();
                                    }
                                }

                                @Override
                                public void onFailure(Call<Expense> call, Throwable t) {}
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDatePicker(EditText target) {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            String m = (month + 1 < 10) ? "0" + (month + 1) : String.valueOf(month + 1);
            String d = (dayOfMonth < 10) ? "0" + dayOfMonth : String.valueOf(dayOfMonth);
            target.setText(year + "-" + m + "-" + d);
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }
}
