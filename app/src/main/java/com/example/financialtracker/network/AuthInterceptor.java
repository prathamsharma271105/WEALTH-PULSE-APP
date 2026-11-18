package com.example.financialtracker.network;

import android.content.Context;
import androidx.annotation.NonNull;
import com.example.financialtracker.utils.SessionManager;
import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {
    private final Context context;

    public AuthInterceptor(Context context) {
        this.context = context.getApplicationContext();
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request original = chain.request();
        Request.Builder builder = original.newBuilder();

        String token = SessionManager.getInstance(context).getToken();
        if (token != null && !token.trim().isEmpty()) {
            builder.header("Authorization", "Bearer " + token.trim());
        }

        builder.header("Content-Type", "application/json");
        builder.header("Accept", "application/json");

        return chain.proceed(builder.build());
    }
}
