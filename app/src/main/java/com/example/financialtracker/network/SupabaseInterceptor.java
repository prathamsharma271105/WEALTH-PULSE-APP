package com.example.financialtracker.network;

import androidx.annotation.NonNull;
import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class SupabaseInterceptor implements Interceptor {

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request original = chain.request();
        Request.Builder builder = original.newBuilder();

        builder.header("apikey", SupabaseConfig.ANON_KEY);
        builder.header("Authorization", "Bearer " + SupabaseConfig.ANON_KEY);
        builder.header("Content-Type", "application/json");
        builder.header("Accept", "application/json");
        builder.header("Prefer", "return=representation");

        return chain.proceed(builder.build());
    }
}
