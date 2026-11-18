package com.example.financialtracker.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.financialtracker.models.User;
import com.google.gson.Gson;

public class SessionManager {
    private static final String PREF_NAME = "wealthpulse_session";
    private static final String KEY_TOKEN = "jwt_token";
    private static final String KEY_USER = "current_user";
    private static final String KEY_THEME = "selected_theme";
    private static final String KEY_BASE_URL = "api_base_url";

    public static final String THEME_SLATE = "slate";
    public static final String THEME_LIGHT = "light";
    public static final String THEME_INDIGO = "indigo";

    // Default API base URL for Android Emulator & local network -> host PC port 8000
    public static final String DEFAULT_BASE_URL = "http://192.168.0.103:8000/";

    private final SharedPreferences prefs;
    private final Gson gson;
    private static SessionManager instance;

    public SessionManager(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }

    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context);
        }
        return instance;
    }

    public void saveSession(String token, User user) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_TOKEN, token);
        if (user != null) {
            editor.putString(KEY_USER, gson.toJson(user));
        }
        editor.apply();
    }

    public void saveUser(User user) {
        if (user != null) {
            prefs.edit().putString(KEY_USER, gson.toJson(user)).apply();
        }
    }

    public User getUser() {
        String json = prefs.getString(KEY_USER, null);
        if (json == null) return null;
        try {
            return gson.fromJson(json, User.class);
        } catch (Exception e) {
            return null;
        }
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public boolean isLoggedIn() {
        String token = getToken();
        return token != null && !token.trim().isEmpty();
    }

    public void clearSession() {
        prefs.edit().remove(KEY_TOKEN).remove(KEY_USER).apply();
    }

    public String getTheme() {
        return prefs.getString(KEY_THEME, THEME_SLATE);
    }

    public void setTheme(String theme) {
        prefs.edit().putString(KEY_THEME, theme).commit();
    }

    public String getBaseUrl() {
        String url = prefs.getString(KEY_BASE_URL, DEFAULT_BASE_URL);
        if (url == null || url.trim().isEmpty()) {
            url = DEFAULT_BASE_URL;
        }
        if (!url.endsWith("/")) {
            url += "/";
        }
        return url;
    }

    public void setBaseUrl(String url) {
        if (url != null && !url.trim().isEmpty()) {
            if (!url.endsWith("/")) {
                url += "/";
            }
            prefs.edit().putString(KEY_BASE_URL, url.trim()).apply();
        }
    }
}
