package com.example.financialtracker.network;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import okhttp3.Request;
import okio.Timeout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SimpleCall<T> implements Call<T> {
    private static final ExecutorService executor = Executors.newCachedThreadPool();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Callable<Response<T>> callable;
    private boolean executed = false;
    private boolean canceled = false;

    public SimpleCall(Callable<Response<T>> callable) {
        this.callable = callable;
    }

    public static <T> SimpleCall<T> success(T data) {
        return new SimpleCall<>(() -> Response.success(data));
    }

    public static <T> SimpleCall<T> error(int code, String message) {
        return new SimpleCall<>(() -> Response.error(code, okhttp3.ResponseBody.create(okhttp3.MediaType.parse("application/json"), "{\"error\":\"" + message + "\"}")));
    }

    @NonNull
    @Override
    public Response<T> execute() throws IOException {
        synchronized (this) {
            if (executed) throw new IllegalStateException("Already executed");
            executed = true;
        }
        if (canceled) {
            throw new IOException("Canceled");
        }
        try {
            return callable.call();
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    @Override
    public void enqueue(@NonNull Callback<T> callback) {
        synchronized (this) {
            if (executed) throw new IllegalStateException("Already executed");
            executed = true;
        }
        executor.execute(() -> {
            if (canceled) {
                mainHandler.post(() -> callback.onFailure(this, new IOException("Canceled")));
                return;
            }
            try {
                Response<T> resp = callable.call();
                if (resp != null) {
                    mainHandler.post(() -> callback.onResponse(this, resp));
                } else {
                    mainHandler.post(() -> callback.onFailure(this, new IOException("Empty response")));
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onFailure(this, e));
            }
        });
    }

    @Override
    public boolean isExecuted() {
        return executed;
    }

    @Override
    public void cancel() {
        canceled = true;
    }

    @Override
    public boolean isCanceled() {
        return canceled;
    }

    @NonNull
    @Override
    public Call<T> clone() {
        return new SimpleCall<>(callable);
    }

    @NonNull
    @Override
    public Request request() {
        return new Request.Builder().url(SupabaseConfig.REST_BASE_URL).build();
    }

    @NonNull
    @Override
    public Timeout timeout() {
        return Timeout.NONE;
    }
}
