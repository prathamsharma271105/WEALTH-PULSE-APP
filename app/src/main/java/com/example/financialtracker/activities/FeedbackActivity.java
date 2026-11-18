package com.example.financialtracker.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.financialtracker.R;
import com.example.financialtracker.models.ApiResponse;
import com.example.financialtracker.models.FeedbackRequest;
import com.example.financialtracker.network.ApiClient;
import com.example.financialtracker.utils.ThemeManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FeedbackActivity extends AppCompatActivity {

    private ImageView btnBack;
    private EditText etMessage;
    private Button btnSubmit;
    private String currentAppliedTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        currentAppliedTheme = com.example.financialtracker.utils.SessionManager.getInstance(this).getTheme();
        setContentView(R.layout.activity_feedback);

        btnBack = findViewById(R.id.btn_back_feedback);
        etMessage = findViewById(R.id.et_feedback_msg);
        btnSubmit = findViewById(R.id.btn_submit_feedback);

        btnBack.setOnClickListener(v -> finish());
        btnSubmit.setOnClickListener(v -> submitFeedback());
    }

    @Override
    protected void onResume() {
        super.onResume();
        String activeTheme = com.example.financialtracker.utils.SessionManager.getInstance(this).getTheme();
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

    private void submitFeedback() {
        String msg = etMessage.getText().toString().trim();
        if (msg.isEmpty()) {
            etMessage.setError("Please write your feedback or question");
            etMessage.requestFocus();
            return;
        }

        btnSubmit.setEnabled(false);
        btnSubmit.setText("Submitting...");

        ApiClient.getService(this).submitFeedback(new FeedbackRequest(msg)).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                btnSubmit.setEnabled(true);
                btnSubmit.setText("Submit Feedback Ticket");
                if (response.isSuccessful()) {
                    Toast.makeText(FeedbackActivity.this, "Thank you! Your feedback ticket was submitted.", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(FeedbackActivity.this, "Submission failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                btnSubmit.setEnabled(true);
                btnSubmit.setText("Submit Feedback Ticket");
                Toast.makeText(FeedbackActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
