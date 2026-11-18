package com.example.financialtracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.financialtracker.activities.FeedbackActivity;
import com.example.financialtracker.activities.LoginActivity;
import com.example.financialtracker.activities.SettingsActivity;
import com.example.financialtracker.fragments.*;
import com.example.financialtracker.models.User;
import com.example.financialtracker.utils.SessionManager;
import com.example.financialtracker.utils.ThemeManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private TextView tvHeaderGreeting;
    private ImageView btnHeaderFeedback;
    private View btnHeaderProfile;
    private TextView tvHeaderAvatarLetter;
    private BottomNavigationView bottomNav;

    private final Fragment dashboardFragment = new DashboardFragment();
    private final Fragment transactionsFragment = new TransactionsFragment();
    private final Fragment habitsFragment = new HabitsFragment();
    private final Fragment goalsFragment = new GoalsFragment();
    private final Fragment moreFragment = new MoreFragment();
    private String currentAppliedTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);

        SessionManager sm = SessionManager.getInstance(this);
        currentAppliedTheme = sm.getTheme();
        if (!sm.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        View root = findViewById(R.id.main_root);
        if (root != null) {
            androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(root, (v, windowInsets) -> {
                androidx.core.graphics.Insets insets = windowInsets.getInsets(
                        androidx.core.view.WindowInsetsCompat.Type.systemBars()
                );
                v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
                return windowInsets;
            });
        }

        tvHeaderGreeting = findViewById(R.id.tv_header_greeting);
        btnHeaderFeedback = findViewById(R.id.btn_header_feedback);
        btnHeaderProfile = findViewById(R.id.btn_header_profile);
        tvHeaderAvatarLetter = findViewById(R.id.tv_header_avatar_letter);
        bottomNav = findViewById(R.id.bottom_navigation);

        updateHeaderInfo();

        btnHeaderFeedback.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, FeedbackActivity.class)));
        btnHeaderProfile.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SettingsActivity.class)));

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_dashboard) {
                switchFragment(dashboardFragment);
                return true;
            } else if (id == R.id.nav_transactions) {
                switchFragment(transactionsFragment);
                return true;
            } else if (id == R.id.nav_habits) {
                switchFragment(habitsFragment);
                return true;
            } else if (id == R.id.nav_goals) {
                switchFragment(goalsFragment);
                return true;
            } else if (id == R.id.nav_more) {
                switchFragment(moreFragment);
                return true;
            }
            return false;
        });

        // Set default fragment or restore selected tab
        int targetNav = getIntent().getIntExtra("selected_nav_item", R.id.nav_dashboard);
        if (targetNav == R.id.nav_transactions) {
            bottomNav.setSelectedItemId(R.id.nav_transactions);
            switchFragment(transactionsFragment);
        } else if (targetNav == R.id.nav_habits) {
            bottomNav.setSelectedItemId(R.id.nav_habits);
            switchFragment(habitsFragment);
        } else if (targetNav == R.id.nav_goals) {
            bottomNav.setSelectedItemId(R.id.nav_goals);
            switchFragment(goalsFragment);
        } else if (targetNav == R.id.nav_more) {
            bottomNav.setSelectedItemId(R.id.nav_more);
            switchFragment(moreFragment);
        } else {
            if (savedInstanceState == null) {
                switchFragment(dashboardFragment);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        String activeTheme = SessionManager.getInstance(this).getTheme();
        if (currentAppliedTheme != null && !currentAppliedTheme.equalsIgnoreCase(activeTheme)) {
            currentAppliedTheme = activeTheme;
            Intent intent = getIntent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            if (bottomNav != null) {
                intent.putExtra("selected_nav_item", bottomNav.getSelectedItemId());
            }
            finish();
            overridePendingTransition(0, 0);
            startActivity(intent);
            overridePendingTransition(0, 0);
            return;
        }
        updateHeaderInfo();
    }

    private void updateHeaderInfo() {
        User user = SessionManager.getInstance(this).getUser();
        if (user != null) {
            String name = user.getName();
            String firstName = name.contains(" ") ? name.substring(0, name.indexOf(" ")) : name;

            int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            String timeGreeting = hour < 12 ? "Good morning" : hour < 18 ? "Good afternoon" : "Good evening";

            tvHeaderGreeting.setText(timeGreeting + ", " + firstName + " 👋");

            if (!name.isEmpty()) {
                tvHeaderAvatarLetter.setText(String.valueOf(Character.toUpperCase(name.charAt(0))));
            }
        }
    }

    private void switchFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}