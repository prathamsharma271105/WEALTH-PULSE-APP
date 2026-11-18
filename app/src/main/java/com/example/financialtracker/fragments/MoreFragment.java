package com.example.financialtracker.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.financialtracker.R;
import com.example.financialtracker.activities.*;
import com.example.financialtracker.models.*;
import com.example.financialtracker.network.ApiClient;
import com.example.financialtracker.network.ApiService;
import com.example.financialtracker.utils.PdfReportGenerator;
import com.example.financialtracker.utils.SessionManager;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MoreFragment extends Fragment {

    private TextView tvAvatarLetter;
    private TextView tvProfileName;
    private TextView tvProfileEmail;
    private TextView tvProfileRole;
    private TextView tvProfileCurrency;

    private View cardInvestments;
    private View cardAnalytics;
    private View cardDownloadPdf;
    private View cardExportJson;
    private View cardSettings;
    private View cardFeedback;
    private View cardAdmin;
    private Button btnLogout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_more, container, false);

        tvAvatarLetter = view.findViewById(R.id.tv_avatar_letter);
        tvProfileName = view.findViewById(R.id.tv_profile_name);
        tvProfileEmail = view.findViewById(R.id.tv_profile_email);
        tvProfileRole = view.findViewById(R.id.tv_profile_role);
        tvProfileCurrency = view.findViewById(R.id.tv_profile_currency);

        cardInvestments = view.findViewById(R.id.card_nav_investments);
        cardAnalytics = view.findViewById(R.id.card_nav_analytics);
        cardDownloadPdf = view.findViewById(R.id.card_download_pdf);
        cardExportJson = view.findViewById(R.id.card_export_json);
        cardSettings = view.findViewById(R.id.card_nav_settings);
        cardFeedback = view.findViewById(R.id.card_nav_feedback);
        cardAdmin = view.findViewById(R.id.card_nav_admin);
        btnLogout = view.findViewById(R.id.btn_logout);

        bindProfile();

        cardInvestments.setOnClickListener(v -> startActivity(new Intent(requireContext(), InvestmentsActivity.class)));
        cardAnalytics.setOnClickListener(v -> startActivity(new Intent(requireContext(), AnalyticsActivity.class)));
        cardSettings.setOnClickListener(v -> startActivity(new Intent(requireContext(), SettingsActivity.class)));
        cardFeedback.setOnClickListener(v -> startActivity(new Intent(requireContext(), FeedbackActivity.class)));

        cardAdmin.setOnClickListener(v -> startActivity(new Intent(requireContext(), AdminActivity.class)));

        cardDownloadPdf.setOnClickListener(v -> generateAndOpenPdf());
        cardExportJson.setOnClickListener(v -> exportDataJson());

        btnLogout.setOnClickListener(v -> showLogoutConfirm());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        bindProfile();
    }

    private void bindProfile() {
        User user = SessionManager.getInstance(requireContext()).getUser();
        if (user != null) {
            String name = user.getName();
            tvProfileName.setText(name);
            tvProfileEmail.setText(user.getEmail());
            tvProfileCurrency.setText(user.getCurrency());
            tvProfileRole.setText(user.getRole().toUpperCase());

            if (!name.isEmpty()) {
                tvAvatarLetter.setText(String.valueOf(Character.toUpperCase(name.charAt(0))));
            }

            cardAdmin.setVisibility(user.isAdmin() ? View.VISIBLE : View.GONE);
        }
    }

    private void generateAndOpenPdf() {
        Toast.makeText(getContext(), "Generating WealthPulse Statement PDF...", Toast.LENGTH_SHORT).show();
        ApiService api = ApiClient.getService(requireContext());
        User user = SessionManager.getInstance(requireContext()).getUser();

        api.getAnalyticsSummary().enqueue(new Callback<AnalyticsSummary>() {
            @Override
            public void onResponse(Call<AnalyticsSummary> call, Response<AnalyticsSummary> response) {
                AnalyticsSummary summary = response.body();

                api.getIncomes().enqueue(new Callback<List<Income>>() {
                    @Override
                    public void onResponse(Call<List<Income>> call, Response<List<Income>> response2) {
                        List<Income> incomes = response2.body();

                        api.getExpenses(null).enqueue(new Callback<List<Expense>>() {
                            @Override
                            public void onResponse(Call<List<Expense>> call, Response<List<Expense>> response3) {
                                List<Expense> expenses = response3.body();

                                api.getGoals().enqueue(new Callback<List<Goal>>() {
                                    @Override
                                    public void onResponse(Call<List<Goal>> call, Response<List<Goal>> response4) {
                                        List<Goal> goals = response4.body();

                                        api.getHabits().enqueue(new Callback<List<Habit>>() {
                                            @Override
                                            public void onResponse(Call<List<Habit>> call, Response<List<Habit>> response5) {
                                                List<Habit> habits = response5.body();

                                                if (!isAdded()) return;
                                                android.content.Context ctx = getContext();
                                                if (ctx == null) return;
                                                try {
                                                    File pdf = PdfReportGenerator.generateReport(
                                                            ctx, user, summary, incomes, expenses, goals, habits, null
                                                    );
                                                    PdfReportGenerator.openPdfFile(ctx, pdf);
                                                } catch (Exception e) {
                                                    Toast.makeText(ctx, "PDF error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            }

                                            @Override
                                            public void onFailure(Call<List<Habit>> call, Throwable t) {}
                                        });
                                    }

                                    @Override
                                    public void onFailure(Call<List<Goal>> call, Throwable t) {}
                                });
                            }

                            @Override
                            public void onFailure(Call<List<Expense>> call, Throwable t) {}
                        });
                    }

                    @Override
                    public void onFailure(Call<List<Income>> call, Throwable t) {}
                });
            }

            @Override
            public void onFailure(Call<AnalyticsSummary> call, Throwable t) {
                Toast.makeText(getContext(), "Failed to load summary for PDF", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void exportDataJson() {
        Toast.makeText(getContext(), "Fetching complete financial backup...", Toast.LENGTH_SHORT).show();
        ApiClient.getService(requireContext()).exportAllData().enqueue(new Callback<ExportDataResponse>() {
            @Override
            public void onResponse(Call<ExportDataResponse> call, Response<ExportDataResponse> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    String json = new GsonBuilder().setPrettyPrinting().create().toJson(response.body());
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, "WealthPulse_Financial_Backup.json");
                    shareIntent.putExtra(Intent.EXTRA_TEXT, json);
                    startActivity(Intent.createChooser(shareIntent, "Share or Save WealthPulse JSON Backup"));
                } else {
                    Toast.makeText(getContext(), "Export failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ExportDataResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Export failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLogoutConfirm() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out of WealthPulse?")
                .setPositiveButton("Log Out", (dialog, which) -> {
                    SessionManager.getInstance(requireContext()).clearSession();
                    Intent intent = new Intent(requireContext(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    requireActivity().finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
