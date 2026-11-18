package com.example.financialtracker.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.financialtracker.MainActivity;
import com.example.financialtracker.R;
import com.example.financialtracker.models.ApiResponse;
import com.example.financialtracker.models.AuthResponse;
import com.example.financialtracker.models.LoginRequest;
import com.example.financialtracker.network.ApiClient;
import com.example.financialtracker.network.ApiService;
import com.example.financialtracker.utils.PasswordUtils;
import com.example.financialtracker.utils.SessionManager;
import com.example.financialtracker.utils.ThemeManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail;
    private EditText etPassword;
    private ImageView btnTogglePassword;
    private Button btnLogin;
    private ProgressBar pbLoading;
    private TextView tvGoRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnTogglePassword = findViewById(R.id.btn_toggle_password);
        btnLogin = findViewById(R.id.btn_login);
        pbLoading = findViewById(R.id.pb_loading);
        tvGoRegister = findViewById(R.id.tv_go_register);

        PasswordUtils.setupPasswordToggle(etPassword, btnTogglePassword);

        btnLogin.setOnClickListener(v -> attemptLogin());

        tvGoRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    private void attemptLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        setLoading(true);

        ApiService api = ApiClient.getService(this);
        LoginRequest req = new LoginRequest(email, password);

        api.login(req).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse auth = response.body();
                    SessionManager.getInstance(LoginActivity.this).saveSession(auth.getToken(), auth.getUser());
                    Toast.makeText(LoginActivity.this, "Welcome back, " + auth.getUser().getName() + "!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    String msg = "Invalid email or password";
                    try {
                        if (response.errorBody() != null) {
                            msg = response.errorBody().string();
                        }
                    } catch (Exception ignored) {}
                    Toast.makeText(LoginActivity.this, "Login Failed: " + msg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                setLoading(false);
                String msg = t != null && t.getMessage() != null ? t.getMessage() : "";
                if (msg.toLowerCase().contains("account not found")) {
                    etEmail.setError("Account not found");
                    etEmail.requestFocus();
                    Toast.makeText(LoginActivity.this, "Account not found with this email. Please register first.", Toast.LENGTH_SHORT).show();
                } else if (msg.toLowerCase().contains("invalid password")) {
                    etPassword.setError("Incorrect password");
                    etPassword.requestFocus();
                    Toast.makeText(LoginActivity.this, "Incorrect password. Please try again.", Toast.LENGTH_SHORT).show();
                } else if (!msg.trim().isEmpty()) {
                    Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LoginActivity.this, "Login failed. Please check your credentials.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setLoading(boolean loading) {
        pbLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!loading);
        btnLogin.setText(loading ? "Authenticating..." : "Sign In to WealthPulse");
    }
}
