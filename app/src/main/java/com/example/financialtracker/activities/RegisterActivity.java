package com.example.financialtracker.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.financialtracker.MainActivity;
import com.example.financialtracker.R;
import com.example.financialtracker.models.AuthResponse;
import com.example.financialtracker.models.RegisterRequest;
import com.example.financialtracker.network.ApiClient;
import com.example.financialtracker.network.ApiService;
import com.example.financialtracker.utils.PasswordUtils;
import com.example.financialtracker.utils.SessionManager;
import com.example.financialtracker.utils.ThemeManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText etName;
    private EditText etEmail;
    private EditText etPassword;
    private ImageView btnToggleRegPassword;
    private EditText etPhone;
    private Spinner spCurrency;
    private Button btnRegister;
    private ProgressBar pbLoading;
    private TextView tvGoLogin;

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
        setContentView(R.layout.activity_register);

        etName = findViewById(R.id.et_reg_name);
        etEmail = findViewById(R.id.et_reg_email);
        etPassword = findViewById(R.id.et_reg_password);
        btnToggleRegPassword = findViewById(R.id.btn_toggle_reg_password);
        etPhone = findViewById(R.id.et_reg_phone);
        spCurrency = findViewById(R.id.sp_reg_currency);
        btnRegister = findViewById(R.id.btn_register);
        pbLoading = findViewById(R.id.pb_reg_loading);
        tvGoLogin = findViewById(R.id.tv_go_login);

        PasswordUtils.setupPasswordToggle(etPassword, btnToggleRegPassword);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, currencies);
        spCurrency.setAdapter(adapter);

        btnRegister.setOnClickListener(v -> attemptRegister());

        tvGoLogin.setOnClickListener(v -> finish());
    }

    private void attemptRegister() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String currency = currencyCodes[spCurrency.getSelectedItemPosition()];

        double incomeTarget = 0.0;
        double savingsTarget = 0.0;

        if (name.length() < 2) {
            etName.setError("Name must be at least 2 characters");
            etName.requestFocus();
            return;
        }

        if (email.isEmpty() || !email.contains("@")) {
            etEmail.setError("Valid email is required");
            etEmail.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return;
        }

        setLoading(true);

        ApiService api = ApiClient.getService(this);
        RegisterRequest req = new RegisterRequest(name, email, password, currency, phone, incomeTarget, savingsTarget);

        api.register(req).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse auth = response.body();
                    SessionManager.getInstance(RegisterActivity.this).saveSession(auth.getToken(), auth.getUser());
                    Toast.makeText(RegisterActivity.this, "Account created successfully!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    String msg = "Registration failed";
                    try {
                        if (response.errorBody() != null) {
                            msg = response.errorBody().string();
                        }
                    } catch (Exception ignored) {}
                    Toast.makeText(RegisterActivity.this, "Error: " + msg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                setLoading(false);
                String msg = t != null && t.getMessage() != null ? t.getMessage() : "Registration failed";
                if (msg.toLowerCase().contains("already exists")) {
                    etEmail.setError("Email already registered");
                    etEmail.requestFocus();
                    Toast.makeText(RegisterActivity.this, "An account with this email already exists.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(RegisterActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setLoading(boolean loading) {
        pbLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnRegister.setEnabled(!loading);
        btnRegister.setText(loading ? "Creating Account..." : "Create WealthPulse Account");
    }
}
