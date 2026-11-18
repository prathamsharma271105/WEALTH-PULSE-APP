package com.example.financialtracker.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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
import com.example.financialtracker.models.ApiResponse;
import com.example.financialtracker.models.Goal;
import com.example.financialtracker.models.GoalContributeRequest;
import com.example.financialtracker.models.GoalRequest;
import com.example.financialtracker.models.User;
import com.example.financialtracker.network.ApiClient;
import com.example.financialtracker.utils.SessionManager;
import java.util.Calendar;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GoalsFragment extends Fragment {

    private SwipeRefreshLayout swipeRefresh;
    private Button btnCreateGoal;
    private RecyclerView rvGoals;
    private TextView tvNoGoals;

    private Button btnTplEmergency;
    private Button btnTplVacation;
    private Button btnTplTech;
    private Button btnTplVehicle;

    private GoalAdapter adapter;
    private String currency = "INR";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_goals, container, false);

        swipeRefresh = view.findViewById(R.id.swipe_refresh_goals);
        btnCreateGoal = view.findViewById(R.id.btn_create_goal);
        rvGoals = view.findViewById(R.id.rv_all_goals);
        tvNoGoals = view.findViewById(R.id.tv_no_goals_msg);

        btnTplEmergency = view.findViewById(R.id.btn_goal_tpl_emergency);
        btnTplVacation = view.findViewById(R.id.btn_goal_tpl_vacation);
        btnTplTech = view.findViewById(R.id.btn_goal_tpl_tech);
        btnTplVehicle = view.findViewById(R.id.btn_goal_tpl_vehicle);

        User user = SessionManager.getInstance(requireContext()).getUser();
        if (user != null) {
            currency = user.getCurrency();
        }

        setupRecyclerView();

        swipeRefresh.setOnRefreshListener(this::loadGoals);
        btnCreateGoal.setOnClickListener(v -> showCreateGoalDialog(null, 0));

        btnTplEmergency.setOnClickListener(v -> createGoalDirect("Emergency Fund (6 Months)", 150000, 0, null));
        btnTplVacation.setOnClickListener(v -> createGoalDirect("Annual Vacation & Travel", 60000, 0, null));
        btnTplTech.setOnClickListener(v -> createGoalDirect("Workstation & Tech Upgrades", 90000, 0, null));
        btnTplVehicle.setOnClickListener(v -> createGoalDirect("Vehicle Down Payment", 200000, 0, null));

        loadGoals();

        return view;
    }

    private void setupRecyclerView() {
        rvGoals.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new GoalAdapter(requireContext(), currency, new GoalAdapter.GoalActionListener() {
            @Override
            public void onContribute(Goal goal) {
                showContributeDialog(goal);
            }

            @Override
            public void onDelete(Goal goal) {
                deleteGoal(goal);
            }
        });
        rvGoals.setAdapter(adapter);
    }

    public void loadGoals() {
        swipeRefresh.setRefreshing(true);
        ApiClient.getService(requireContext()).getGoals().enqueue(new Callback<List<Goal>>() {
            @Override
            public void onResponse(Call<List<Goal>> call, Response<List<Goal>> response) {
                if (isAdded()) {
                    swipeRefresh.setRefreshing(false);
                    if (response.isSuccessful() && response.body() != null) {
                        List<Goal> list = response.body();
                        adapter.setGoals(list);
                        tvNoGoals.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Goal>> call, Throwable t) {
                if (isAdded()) swipeRefresh.setRefreshing(false);
            }
        });
    }

    private void showContributeDialog(Goal goal) {
        View dView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_contribution, null);
        EditText etAmount = dView.findViewById(R.id.et_contrib_amount);

        new AlertDialog.Builder(requireContext())
                .setTitle("Add Funds to " + goal.getTitle())
                .setView(dView)
                .setPositiveButton("Contribute", (dialog, which) -> {
                    String amtStr = etAmount.getText().toString().trim();
                    if (amtStr.isEmpty()) return;
                    double amt = parseAmount(amtStr);
                    if (amt <= 0) return;

                    ApiClient.getService(requireContext()).contributeGoal(goal.getId(), new GoalContributeRequest(amt))
                            .enqueue(new Callback<Goal>() {
                                @Override
                                public void onResponse(Call<Goal> call, Response<Goal> response) {
                                    if (response.isSuccessful()) {
                                        Toast.makeText(getContext(), "Contribution recorded!", Toast.LENGTH_SHORT).show();
                                        loadGoals();
                                    }
                                }

                                @Override
                                public void onFailure(Call<Goal> call, Throwable t) {
                                    Toast.makeText(getContext(), "Contribution failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteGoal(Goal goal) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Goal")
                .setMessage("Delete milestone '" + goal.getTitle() + "'?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    ApiClient.getService(requireContext()).deleteGoal(goal.getId()).enqueue(new Callback<ApiResponse>() {
                        @Override
                        public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                            Toast.makeText(getContext(), "Goal removed", Toast.LENGTH_SHORT).show();
                            loadGoals();
                        }

                        @Override
                        public void onFailure(Call<ApiResponse> call, Throwable t) {
                            Toast.makeText(getContext(), "Delete failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void createGoalDirect(String title, double target, double saved, @Nullable String deadline) {
        ApiClient.getService(requireContext()).addGoal(new GoalRequest(title, target, saved, deadline))
                .enqueue(new Callback<Goal>() {
                    @Override
                    public void onResponse(Call<Goal> call, Response<Goal> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(getContext(), "Goal created: " + title, Toast.LENGTH_SHORT).show();
                            loadGoals();
                        }
                    }

                    @Override
                    public void onFailure(Call<Goal> call, Throwable t) {
                        Toast.makeText(getContext(), "Failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showCreateGoalDialog(@Nullable String defTitle, double defTarget) {
        View dView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_create_goal, null);
        EditText etTitle = dView.findViewById(R.id.et_goal_title);
        EditText etTarget = dView.findViewById(R.id.et_goal_target);
        EditText etSaved = dView.findViewById(R.id.et_goal_saved);
        EditText etDeadline = dView.findViewById(R.id.et_goal_deadline);

        if (defTitle != null) etTitle.setText(defTitle);
        if (defTarget > 0) etTarget.setText(String.valueOf(defTarget));

        etDeadline.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
                String m = (month + 1 < 10) ? "0" + (month + 1) : String.valueOf(month + 1);
                String d = (dayOfMonth < 10) ? "0" + dayOfMonth : String.valueOf(dayOfMonth);
                etDeadline.setText(year + "-" + m + "-" + d);
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        new AlertDialog.Builder(requireContext())
                .setTitle("Create Savings Milestone")
                .setView(dView)
                .setPositiveButton("Save Goal", (dialog, which) -> {
                    String title = etTitle.getText().toString().trim();
                    String tgtStr = etTarget.getText().toString().trim();
                    String savStr = etSaved.getText().toString().trim();
                    String deadline = etDeadline.getText().toString().trim();

                    if (title.isEmpty() || tgtStr.isEmpty()) {
                        Toast.makeText(getContext(), "Title and target amount are required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double target = parseAmount(tgtStr);
                    double saved = parseAmount(savStr);

                    if (target <= 0) {
                        Toast.makeText(getContext(), "Target amount must be greater than zero", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    createGoalDirect(title, target, saved, deadline.isEmpty() ? null : deadline);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private double parseAmount(String input) {
        if (input == null || input.trim().isEmpty()) return 0;
        try {
            String clean = input.replaceAll("[^0-9.]", "").trim();
            return clean.isEmpty() ? 0 : Double.parseDouble(clean);
        } catch (Exception e) {
            return 0;
        }
    }
}
