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
import com.example.financialtracker.adapters.TransactionAdapter;
import com.example.financialtracker.models.*;
import com.example.financialtracker.network.ApiClient;
import com.example.financialtracker.network.ApiService;
import com.example.financialtracker.utils.FormatUtils;
import com.example.financialtracker.utils.SessionManager;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TransactionsFragment extends Fragment {

    private SwipeRefreshLayout swipeRefresh;
    private TextView tvTotalIncome;
    private TextView tvTotalExpense;
    private TextView tvNetBalance;
    private Button btnAddIncome;
    private Button btnAddExpense;
    private Button btnFilterAll;
    private Button btnFilterIncome;
    private Button btnFilterExpense;
    private RecyclerView rvTransactions;
    private TextView tvNoTransactions;

    private TransactionAdapter adapter;
    private String currency = "INR";
    private int currentFilter = 0; // 0 = All, 1 = Income, 2 = Expense

    private final List<Income> rawIncomes = new ArrayList<>();
    private final List<Expense> rawExpenses = new ArrayList<>();

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
        View view = inflater.inflate(R.layout.fragment_transactions, container, false);

        swipeRefresh = view.findViewById(R.id.swipe_refresh_transactions);
        tvTotalIncome = view.findViewById(R.id.tv_ledger_total_income);
        tvTotalExpense = view.findViewById(R.id.tv_ledger_total_expense);
        tvNetBalance = view.findViewById(R.id.tv_ledger_net_balance);
        btnAddIncome = view.findViewById(R.id.btn_trans_add_income);
        btnAddExpense = view.findViewById(R.id.btn_trans_add_expense);
        btnFilterAll = view.findViewById(R.id.btn_filter_all);
        btnFilterIncome = view.findViewById(R.id.btn_filter_income);
        btnFilterExpense = view.findViewById(R.id.btn_filter_expense);
        rvTransactions = view.findViewById(R.id.rv_transactions);
        tvNoTransactions = view.findViewById(R.id.tv_no_transactions);

        User user = SessionManager.getInstance(requireContext()).getUser();
        if (user != null) {
            currency = user.getCurrency();
        }

        setupRecyclerView();

        swipeRefresh.setOnRefreshListener(this::loadTransactions);

        btnAddIncome.setOnClickListener(v -> showAddIncomeDialog());
        btnAddExpense.setOnClickListener(v -> showAddExpenseDialog());

        btnFilterAll.setOnClickListener(v -> setFilter(0));
        btnFilterIncome.setOnClickListener(v -> setFilter(1));
        btnFilterExpense.setOnClickListener(v -> setFilter(2));

        loadTransactions();

        return view;
    }

    private void setupRecyclerView() {
        rvTransactions.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new TransactionAdapter(requireContext(), currency, item -> deleteTransaction(item));
        rvTransactions.setAdapter(adapter);
    }

    private void setFilter(int filter) {
        currentFilter = filter;
        updateFilterButtons();
        applyFilterAndRender();
    }

    private void updateFilterButtons() {
        btnFilterAll.setBackgroundResource(currentFilter == 0 ? R.drawable.btn_primary : R.drawable.btn_ghost);
        btnFilterAll.setTextColor(currentFilter == 0 ? androidx.core.content.ContextCompat.getColor(requireContext(), R.color.white) : androidx.core.content.ContextCompat.getColor(requireContext(), R.color.slate_text_muted));

        btnFilterIncome.setBackgroundResource(currentFilter == 1 ? R.drawable.btn_success : R.drawable.btn_ghost);
        btnFilterIncome.setTextColor(currentFilter == 1 ? androidx.core.content.ContextCompat.getColor(requireContext(), R.color.white) : androidx.core.content.ContextCompat.getColor(requireContext(), R.color.slate_text_muted));

        btnFilterExpense.setBackgroundResource(currentFilter == 2 ? R.drawable.btn_danger : R.drawable.btn_ghost);
        btnFilterExpense.setTextColor(currentFilter == 2 ? androidx.core.content.ContextCompat.getColor(requireContext(), R.color.white) : androidx.core.content.ContextCompat.getColor(requireContext(), R.color.slate_text_muted));
    }

    public void loadTransactions() {
        swipeRefresh.setRefreshing(true);
        ApiService api = ApiClient.getService(requireContext());

        api.getIncomes().enqueue(new Callback<List<Income>>() {
            @Override
            public void onResponse(Call<List<Income>> call, Response<List<Income>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    rawIncomes.clear();
                    rawIncomes.addAll(response.body());
                }

                // Fetch expenses next
                api.getExpenses(null).enqueue(new Callback<List<Expense>>() {
                    @Override
                    public void onResponse(Call<List<Expense>> call2, Response<List<Expense>> response2) {
                        if (isAdded()) {
                            swipeRefresh.setRefreshing(false);
                            if (response2.isSuccessful() && response2.body() != null) {
                                rawExpenses.clear();
                                rawExpenses.addAll(response2.body());
                            }
                            updateSummaryBanner();
                            applyFilterAndRender();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Expense>> call2, Throwable t2) {
                        if (isAdded()) {
                            swipeRefresh.setRefreshing(false);
                            applyFilterAndRender();
                        }
                    }
                });
            }

            @Override
            public void onFailure(Call<List<Income>> call, Throwable t) {
                if (isAdded()) swipeRefresh.setRefreshing(false);
            }
        });
    }

    private void updateSummaryBanner() {
        double incSum = 0;
        for (Income i : rawIncomes) incSum += i.getAmount();

        double expSum = 0;
        for (Expense e : rawExpenses) expSum += e.getAmount();

        double net = incSum - expSum;

        tvTotalIncome.setText("+" + FormatUtils.formatCurrency(incSum, currency));
        tvTotalExpense.setText("-" + FormatUtils.formatCurrency(expSum, currency));
        tvNetBalance.setText(FormatUtils.formatCurrency(net, currency));
    }

    private void applyFilterAndRender() {
        List<TransactionAdapter.TransactionItem> list = new ArrayList<>();

        if (currentFilter == 0 || currentFilter == 1) {
            for (Income inc : rawIncomes) {
                list.add(TransactionAdapter.TransactionItem.fromIncome(inc));
            }
        }

        if (currentFilter == 0 || currentFilter == 2) {
            for (Expense exp : rawExpenses) {
                list.add(TransactionAdapter.TransactionItem.fromExpense(exp));
            }
        }

        // Sort by date descending
        Collections.sort(list, (a, b) -> {
            if (a.date == null) return 1;
            if (b.date == null) return -1;
            return b.date.compareTo(a.date);
        });

        adapter.setItems(list);
        tvNoTransactions.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void deleteTransaction(TransactionAdapter.TransactionItem item) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete " + (item.isIncome ? "Income" : "Expense"))
                .setMessage("Are you sure you want to remove '" + item.title + "'?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    ApiService api = ApiClient.getService(requireContext());
                    Callback<ApiResponse> cb = new Callback<ApiResponse>() {
                        @Override
                        public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                            Toast.makeText(getContext(), "Transaction deleted", Toast.LENGTH_SHORT).show();
                            loadTransactions();
                        }

                        @Override
                        public void onFailure(Call<ApiResponse> call, Throwable t) {}
                    };

                    if (item.isIncome) {
                        api.deleteIncome(item.id).enqueue(cb);
                    } else {
                        api.deleteExpense(item.id).enqueue(cb);
                    }
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
                .setTitle("+ Add Income Record")
                .setView(dView)
                .setPositiveButton("Save Income", (dialog, which) -> {
                    String source = etSource.getText().toString().trim();
                    String amtStr = etAmount.getText().toString().trim();
                    String date = etDate.getText().toString().trim();
                    String note = etNote.getText().toString().trim();

                    if (source.isEmpty() || amtStr.isEmpty()) {
                        Toast.makeText(getContext(), "Source and amount are required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double amount = Double.parseDouble(amtStr);
                    ApiClient.getService(requireContext()).addIncome(new IncomeRequest(source, amount, date, note))
                            .enqueue(new Callback<Income>() {
                                @Override
                                public void onResponse(Call<Income> call, Response<Income> response) {
                                    if (response.isSuccessful()) {
                                        Toast.makeText(getContext(), "Income saved successfully!", Toast.LENGTH_SHORT).show();
                                        loadTransactions();
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
                                        Toast.makeText(getContext(), "Expense saved successfully!", Toast.LENGTH_SHORT).show();
                                        loadTransactions();
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
