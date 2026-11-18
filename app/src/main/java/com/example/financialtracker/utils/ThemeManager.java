package com.example.financialtracker.utils;

import android.app.Activity;
import com.example.financialtracker.R;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ThemeManager {

    public interface ThemeChangeListener {
        void onThemeChanged(String newTheme);
    }

    private static final List<ThemeChangeListener> listeners = new CopyOnWriteArrayList<>();

    public static void registerListener(ThemeChangeListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public static void unregisterListener(ThemeChangeListener listener) {
        if (listener != null) {
            listeners.remove(listener);
        }
    }

    public static void notifyThemeChanged(String newTheme) {
        for (ThemeChangeListener listener : listeners) {
            if (listener != null) {
                try {
                    listener.onThemeChanged(newTheme);
                } catch (Exception ignored) {}
            }
        }
    }

    public static void applyTheme(Activity activity) {
        SessionManager sm = SessionManager.getInstance(activity);
        String theme = sm.getTheme();
        if (SessionManager.THEME_LIGHT.equalsIgnoreCase(theme)) {
            activity.setTheme(R.style.Theme_WealthPulse_CleanLight);
        } else if (SessionManager.THEME_INDIGO.equalsIgnoreCase(theme)) {
            activity.setTheme(R.style.Theme_WealthPulse_ExecutiveIndigo);
        } else {
            activity.setTheme(R.style.Theme_WealthPulse_DarkSlate);
        }
    }
}

