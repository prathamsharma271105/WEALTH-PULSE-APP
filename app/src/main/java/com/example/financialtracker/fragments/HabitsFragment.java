package com.example.financialtracker.fragments;

import android.app.AlertDialog;
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
import com.example.financialtracker.adapters.HabitAdapter;
import com.example.financialtracker.models.ApiResponse;
import com.example.financialtracker.models.Habit;
import com.example.financialtracker.models.HabitRequest;
import com.example.financialtracker.models.HabitToggleResponse;
import com.example.financialtracker.models.User;
import com.example.financialtracker.network.ApiClient;
import com.example.financialtracker.network.ApiService;
import com.example.financialtracker.utils.SessionManager;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HabitsFragment extends Fragment {

    private SwipeRefreshLayout swipeRefresh;
    private Button btnCreateHabit;
    private RecyclerView rvHabits;
    private TextView tvNoHabits;

    private Button btnTplSave200;
    private Button btnTplLogDaily;
    private Button btnTplWeeklySip;
    private Button btnTplMonthlySub;

    private HabitAdapter adapter;
    private String currency = "INR";

    private final String[] frequencies = new String[]{"Daily Habit", "Weekly Routine", "Monthly Checkpoint"};
    private final String[] frequencyValues = new String[]{"daily", "weekly", "monthly"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_habits, container, false);

        swipeRefresh = view.findViewById(R.id.swipe_refresh_habits);
        btnCreateHabit = view.findViewById(R.id.btn_create_habit);
        rvHabits = view.findViewById(R.id.rv_all_habits);
        tvNoHabits = view.findViewById(R.id.tv_no_habits_msg);

        btnTplSave200 = view.findViewById(R.id.btn_template_save200);
        btnTplLogDaily = view.findViewById(R.id.btn_template_log_daily);
        btnTplWeeklySip = view.findViewById(R.id.btn_template_weekly_sip);
        btnTplMonthlySub = view.findViewById(R.id.btn_template_monthly_sub);

        User user = SessionManager.getInstance(requireContext()).getUser();
        if (user != null) {
            currency = user.getCurrency();
        }

        setupRecyclerView();

        swipeRefresh.setOnRefreshListener(this::loadHabits);
        btnCreateHabit.setOnClickListener(v -> showCreateHabitDialog(null, "daily", 0));

        btnTplSave200.setOnClickListener(v -> createHabitDirect("Auto-save ₹200 into Emergency Fund", "daily", 200));
        btnTplLogDaily.setOnClickListener(v -> createHabitDirect("Log all daily transactions before bed", "daily", 0));
        btnTplWeeklySip.setOnClickListener(v -> createHabitDirect("Transfer ₹1,500 to Index Fund SIP", "weekly", 1500));
        btnTplMonthlySub.setOnClickListener(v -> createHabitDirect("Review monthly subscription charges", "monthly", 0));

        loadHabits();

        return view;
    }

    private void setupRecyclerView() {
        rvHabits.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new HabitAdapter(requireContext(), currency, new HabitAdapter.HabitActionListener() {
            @Override
            public void onToggle(Habit habit, boolean isChecked) {
                toggleHabit(habit);
            }

            @Override
            public void onDelete(Habit habit) {
                deleteHabit(habit);
            }
        });
        rvHabits.setAdapter(adapter);
    }

    public void loadHabits() {
        swipeRefresh.setRefreshing(true);
        ApiClient.getService(requireContext()).getHabits().enqueue(new Callback<List<Habit>>() {
            @Override
            public void onResponse(Call<List<Habit>> call, Response<List<Habit>> response) {
                if (isAdded()) {
                    swipeRefresh.setRefreshing(false);
                    if (response.isSuccessful() && response.body() != null) {
                        List<Habit> list = response.body();
                        adapter.setHabits(list);
                        tvNoHabits.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Habit>> call, Throwable t) {
                if (isAdded()) swipeRefresh.setRefreshing(false);
            }
        });
    }

    private void toggleHabit(Habit habit) {
        ApiClient.getService(requireContext()).toggleHabit(habit.getId()).enqueue(new Callback<HabitToggleResponse>() {
            @Override
            public void onResponse(Call<HabitToggleResponse> call, Response<HabitToggleResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    HabitToggleResponse res = response.body();
                    habit.setCompletedToday(res.isCompletedToday());
                    habit.setStreak(res.getStreak());
                    habit.setTotalCompletions(res.getTotalCompletions());
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<HabitToggleResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Sync failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteHabit(Habit habit) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Habit")
                .setMessage("Delete '" + habit.getName() + "' and its streak?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    ApiClient.getService(requireContext()).deleteHabit(habit.getId()).enqueue(new Callback<ApiResponse>() {
                        @Override
                        public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                            Toast.makeText(getContext(), "Habit removed", Toast.LENGTH_SHORT).show();
                            loadHabits();
                        }

                        @Override
                        public void onFailure(Call<ApiResponse> call, Throwable t) {}
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void createHabitDirect(String name, String frequency, double target) {
        ApiClient.getService(requireContext()).addHabit(new HabitRequest(name, frequency, target))
                .enqueue(new Callback<Habit>() {
                    @Override
                    public void onResponse(Call<Habit> call, Response<Habit> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(getContext(), "Habit created: " + name, Toast.LENGTH_SHORT).show();
                            loadHabits();
                        }
                    }

                    @Override
                    public void onFailure(Call<Habit> call, Throwable t) {
                        Toast.makeText(getContext(), "Failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showCreateHabitDialog(@Nullable String defName, String defFreq, double defTarget) {
        View dView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_create_habit, null);
        EditText etName = dView.findViewById(R.id.et_habit_name);
        Spinner spFreq = dView.findViewById(R.id.sp_habit_frequency);
        EditText etTarget = dView.findViewById(R.id.et_habit_target);

        ArrayAdapter<String> freqAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, frequencies);
        spFreq.setAdapter(freqAdapter);

        if (defName != null) etName.setText(defName);
        if (defTarget > 0) etTarget.setText(String.valueOf(defTarget));

        new AlertDialog.Builder(requireContext())
                .setTitle("Create Financial Habit")
                .setView(dView)
                .setPositiveButton("Create Habit", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(getContext(), "Habit name is required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String frequency = frequencyValues[spFreq.getSelectedItemPosition()];
                    String tgtStr = etTarget.getText().toString().trim();
                    double target = tgtStr.isEmpty() ? 0 : Double.parseDouble(tgtStr);

                    createHabitDirect(name, frequency, target);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
