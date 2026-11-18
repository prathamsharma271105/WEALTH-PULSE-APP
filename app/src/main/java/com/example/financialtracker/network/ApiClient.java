package com.example.financialtracker.network;

import android.content.Context;
import com.example.financialtracker.utils.SessionManager;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static ApiService apiService;
    private static String currentBaseUrl = "";

    public static synchronized ApiService getService(Context context) {
        return SupabaseService.getInstance(context);
    }

    public static synchronized void resetClient() {
        // Reset cached instance if needed
    }
}
