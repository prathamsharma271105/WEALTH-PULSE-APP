package com.example.financialtracker.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.financialtracker.R;
import com.example.financialtracker.models.ApiResponse;
import com.example.financialtracker.models.AuthResponse;
import com.example.financialtracker.models.ChangePasswordRequest;
import com.example.financialtracker.models.UpdateProfileRequest;
import com.example.financialtracker.models.User;
import com.example.financialtracker.network.ApiClient;
import com.example.financialtracker.network.ApiService;
import com.example.financialtracker.utils.SessionManager;
import com.example.financialtracker.utils.ThemeManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingsActivity extends AppCompatActivity {

    private ImageView btnBack;
    private RadioGroup rgThemes;
    private RadioButton rbSlate;
    private RadioButton rbLight;
    private RadioButton rbIndigo;

    private EditText etName;
    private EditText etEmail;
    private EditText etPhone;
    private Button btnSaveProfile;

    private Spinner spCurrency;
    private EditText etIncomeTarget;
    private EditText etSavingsTarget;
    private Button btnSavePreferences;

    private Button btnChangePassword;
    private Button btnDeleteAccount;

    private final String[] currencies = new String[]{
            "INR - Indian Rupee (₹)",
            "USD - US Dollar ($)",
            "EUR - Euro (€)",
            "GBP - British Pound (£)",
            "AED - UAE Dirham (AED)",
            "SGD - Singapore Dollar (S$)",
            "CAD - Canadian Dollar (C$)",
            "AUD - Australian Dollar (A$)",
            "JPY - Japanese Yen (¥)"
    };

    private final String[] currencyCodes = new String[]{
            "INR", "USD", "EUR", "GBP", "AED", "SGD", "CAD", "AUD", "JPY"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        btnBack = findViewById(R.id.btn_back_settings);
        rgThemes = findViewById(R.id.rg_themes);
        rbSlate = findViewById(R.id.rb_theme_slate);
        rbLight = findViewById(R.id.rb_theme_light);
        rbIndigo = findViewById(R.id.rb_theme_indigo);

        etName = findViewById(R.id.et_settings_name);
        etEmail = findViewById(R.id.et_settings_email);
        etPhone = findViewById(R.id.et_settings_phone);
        btnSaveProfile = findViewById(R.id.btn_save_profile);

        spCurrency = findViewById(R.id.sp_settings_currency);
        etIncomeTarget = findViewById(R.id.et_settings_income_target);
        etSavingsTarget = findViewById(R.id.et_settings_savings_target);
        btnSavePreferences = findViewById(R.id.btn_save_preferences);

        btnChangePassword = findViewById(R.id.btn_change_password);
        btnDeleteAccount = findViewById(R.id.btn_delete_account);

        btnBack.setOnClickListener(v -> finish());

        setupThemes();
        setupSpinners();
        loadUserDetails();

        btnSaveProfile.setOnClickListener(v -> saveProfile());
        btnSavePreferences.setOnClickListener(v -> savePreferences());
        btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());
        btnDeleteAccount.setOnClickListener(v -> showDeleteAccountConfirm());
    }

    private void setupThemes() {
        SessionManager sm = SessionManager.getInstance(this);
        String currentTheme = sm.getTheme();

        rgThemes.setOnCheckedChangeListener(null);

        if (SessionManager.THEME_LIGHT.equalsIgnoreCase(currentTheme)) {
            rbLight.setChecked(true);
        } else if (SessionManager.THEME_INDIGO.equalsIgnoreCase(currentTheme)) {
            rbIndigo.setChecked(true);
        } else {
            rbSlate.setChecked(true);
        }

        rgThemes.setOnCheckedChangeListener((group, checkedId) -> {
            String newTheme = SessionManager.THEME_SLATE;
            if (checkedId == R.id.rb_theme_light) {
                newTheme = SessionManager.THEME_LIGHT;
            } else if (checkedId == R.id.rb_theme_indigo) {
                newTheme = SessionManager.THEME_INDIGO;
            }

            if (!newTheme.equalsIgnoreCase(sm.getTheme())) {
                sm.setTheme(newTheme);
                ThemeManager.notifyThemeChanged(newTheme);
                Intent intent = getIntent();
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                finish();
                overridePendingTransition(0, 0);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });
    }

    private void setupSpinners() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, currencies);
        spCurrency.setAdapter(adapter);
    }

    private void loadUserDetails() {
        User user = SessionManager.getInstance(this).getUser();
        if (user != null) {
            etName.setText(user.getName());
            etEmail.setText(user.getEmail());
            etPhone.setText(user.getPhone());

            if (user.getIncomeTarget() > 0) {
                etIncomeTarget.setText(String.valueOf(user.getIncomeTarget()));
            }
            if (user.getSavingsTarget() > 0) {
                etSavingsTarget.setText(String.valueOf(user.getSavingsTarget()));
            }

            for (int i = 0; i < currencyCodes.length; i++) {
                if (currencyCodes[i].equalsIgnoreCase(user.getCurrency())) {
                    spCurrency.setSelection(i);
                    break;
                }
            }
        }
    }

    private void saveProfile() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        if (name.length() < 2) {
            etName.setError("Name must be at least 2 characters");
            return;
        }

        User current = SessionManager.getInstance(this).getUser();
        double inc = current != null ? current.getIncomeTarget() : 0;
        double sav = current != null ? current.getSavingsTarget() : 0;
        String cur = current != null ? current.getCurrency() : "INR";

        UpdateProfileRequest req = new UpdateProfileRequest(name, email, phone, cur, inc, sav);
        ApiClient.getService(this).updateProfile(req).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getUser() != null) {
                    SessionManager.getInstance(SettingsActivity.this).saveUser(response.body().getUser());
                    Toast.makeText(SettingsActivity.this, "Profile changes saved!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SettingsActivity.this, "Save failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                Toast.makeText(SettingsActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void savePreferences() {
        String cur = currencyCodes[spCurrency.getSelectedItemPosition()];
        String incStr = etIncomeTarget.getText().toString().trim();
        double inc = incStr.isEmpty() ? 0 : Double.parseDouble(incStr);

        String savStr = etSavingsTarget.getText().toString().trim();
        double sav = savStr.isEmpty() ? 0 : Double.parseDouble(savStr);

        User current = SessionManager.getInstance(this).getUser();
        String name = current != null ? current.getName() : "";
        String email = current != null ? current.getEmail() : "";
        String phone = current != null ? current.getPhone() : "";

        UpdateProfileRequest req = new UpdateProfileRequest(name, email, phone, cur, inc, sav);
        ApiClient.getService(this).updateProfile(req).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getUser() != null) {
                    SessionManager.getInstance(SettingsActivity.this).saveUser(response.body().getUser());
                    Toast.makeText(SettingsActivity.this, "Financial preferences saved!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SettingsActivity.this, "Save failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                Toast.makeText(SettingsActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showChangePasswordDialog() {
        android.view.View dView = getLayoutInflater().inflate(R.layout.dialog_change_password, null);
        EditText etCurr = dView.findViewById(R.id.et_curr_pass);
        EditText etNew = dView.findViewById(R.id.et_new_pass);
        android.widget.ImageView btnToggleCurr = dView.findViewById(R.id.btn_toggle_curr_pass);
        android.widget.ImageView btnToggleNew = dView.findViewById(R.id.btn_toggle_new_pass);

        com.example.financialtracker.utils.PasswordUtils.setupPasswordToggle(etCurr, btnToggleCurr);
        com.example.financialtracker.utils.PasswordUtils.setupPasswordToggle(etNew, btnToggleNew);

        new AlertDialog.Builder(this)
                .setTitle("Change Account Password")
                .setView(dView)
                .setPositiveButton("Update Password", (dialog, which) -> {
                    String curr = etCurr.getText().toString().trim();
                    String newP = etNew.getText().toString().trim();

                    if (curr.isEmpty() || newP.length() < 6) {
                        Toast.makeText(this, "New password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    ApiClient.getService(this).changePassword(new ChangePasswordRequest(curr, newP))
                            .enqueue(new Callback<ApiResponse>() {
                                @Override
                                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                                    if (response.isSuccessful()) {
                                        Toast.makeText(SettingsActivity.this, "Password updated successfully!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(SettingsActivity.this, "Incorrect current password", Toast.LENGTH_LONG).show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<ApiResponse> call, Throwable t) {
                                    Toast.makeText(SettingsActivity.this, "Failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDeleteAccountConfirm() {
        new AlertDialog.Builder(this)
                .setTitle("⚠️ Delete WealthPulse Account")
                .setMessage("Are you absolutely sure? All income records, expenses, habits, milestones, and investments will be permanently deleted.")
                .setPositiveButton("PERMANENTLY DELETE", (dialog, which) -> {
                    ApiClient.getService(this).deleteAccount().enqueue(new Callback<ApiResponse>() {
                        @Override
                        public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                            Toast.makeText(SettingsActivity.this, "Account deleted", Toast.LENGTH_LONG).show();
                            SessionManager.getInstance(SettingsActivity.this).clearSession();
                            Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        }

                        @Override
                        public void onFailure(Call<ApiResponse> call, Throwable t) {
                            Toast.makeText(SettingsActivity.this, "Failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Keep Account", null)
                .show();
    }
}
