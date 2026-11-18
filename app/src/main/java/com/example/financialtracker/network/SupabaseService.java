package com.example.financialtracker.network;

import android.content.Context;
import com.example.financialtracker.models.*;
import com.example.financialtracker.utils.FormatUtils;
import com.example.financialtracker.utils.SessionManager;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SupabaseService implements ApiService {

    private final Context context;
    private final SupabaseRawApi rawApi;
    private static SupabaseService instance;

    private SupabaseService(Context context) {
        this.context = context.getApplicationContext();

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new SupabaseInterceptor())
                .addInterceptor(logging)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(SupabaseConfig.REST_BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        this.rawApi = retrofit.create(SupabaseRawApi.class);
    }

    public static synchronized SupabaseService getInstance(Context context) {
        if (instance == null) {
            instance = new SupabaseService(context);
        }
        return instance;
    }

    private String getCurrentUserId() {
        User u = SessionManager.getInstance(context).getUser();
        return u != null ? u.getId() : "1";
    }

    // =========================================================================
    // Authentication & Profile
    // =========================================================================

    @Override
    public Call<AuthResponse> register(RegisterRequest request) {
        return new SimpleCall<>(() -> {
            Response<List<User>> checkResp = rawApi.getUserByEmail("eq." + request.getEmail().trim().toLowerCase()).execute();
            if (checkResp.isSuccessful() && checkResp.body() != null && !checkResp.body().isEmpty()) {
                throw new IOException("An account with email " + request.getEmail() + " already exists.");
            }

            User newUser = new User();
            newUser.setName(request.getName());
            newUser.setEmail(request.getEmail().trim().toLowerCase());
            newUser.setPasswordHash(request.getPassword());
            newUser.setRole("user");
            newUser.setCurrency(request.getCurrency());
            newUser.setPhone(request.getPhone());
            newUser.setIncomeTarget(request.getIncomeTarget());
            newUser.setSavingsTarget(request.getSavingsTarget());

            Response<List<User>> createResp = rawApi.createUser(newUser).execute();
            if (createResp.isSuccessful() && createResp.body() != null && !createResp.body().isEmpty()) {
                User created = createResp.body().get(0);
                String token = "sb_token_" + created.getId();
                return Response.success(new AuthResponse(token, created));
            } else {
                String errDetail = "";
                if (createResp.errorBody() != null) {
                    try {
                        errDetail = " (" + createResp.errorBody().string() + ")";
                    } catch (Exception ignored) {}
                }
                throw new IOException("Registration failed" + errDetail + ". Please verify Supabase tables exist and RLS is disabled.");
            }
        });
    }

    @Override
    public Call<AuthResponse> login(LoginRequest request) {
        return new SimpleCall<>(() -> {
            String email = request.getEmail().trim().toLowerCase();
            Response<List<User>> resp = rawApi.getUserByEmail("eq." + email).execute();

            if (!resp.isSuccessful()) {
                String errDetail = "";
                if (resp.errorBody() != null) {
                    try {
                        errDetail = " (" + resp.errorBody().string() + ")";
                    } catch (Exception ignored) {}
                }
                throw new IOException("Supabase query error: " + resp.code() + errDetail);
            }

            if (resp.body() == null || resp.body().isEmpty()) {
                throw new IOException("Account not found with email " + email + ". Please register first.");
            }

            User user = resp.body().get(0);
            if (!user.getPasswordHash().equals(request.getPassword())) {
                throw new IOException("Invalid password. Please check your credentials.");
            }

            String token = "sb_token_" + user.getId();
            return Response.success(new AuthResponse(token, user));
        });
    }

    @Override
    public Call<AuthResponse> getProfile() {
        return new SimpleCall<>(() -> {
            String uid = getCurrentUserId();
            Response<List<User>> resp = rawApi.getUserById("eq." + uid).execute();
            if (resp.isSuccessful() && resp.body() != null && !resp.body().isEmpty()) {
                User u = resp.body().get(0);
                return Response.success(new AuthResponse("sb_token_" + u.getId(), u));
            }
            throw new IOException("Could not fetch user profile");
        });
    }

    @Override
    public Call<AuthResponse> updateProfile(UpdateProfileRequest request) {
        return new SimpleCall<>(() -> {
            String uid = getCurrentUserId();
            User update = new User();
            if (request.getName() != null) update.setName(request.getName());
            if (request.getPhone() != null) update.setPhone(request.getPhone());
            if (request.getCurrency() != null) update.setCurrency(request.getCurrency());
            if (request.getIncomeTarget() > 0) update.setIncomeTarget(request.getIncomeTarget());
            if (request.getSavingsTarget() > 0) update.setSavingsTarget(request.getSavingsTarget());

            Response<List<User>> resp = rawApi.updateUser("eq." + uid, update).execute();
            if (resp.isSuccessful() && resp.body() != null && !resp.body().isEmpty()) {
                User updated = resp.body().get(0);
                SessionManager.getInstance(context).saveUser(updated);
                return Response.success(new AuthResponse("sb_token_" + updated.getId(), updated));
            }
            throw new IOException("Profile update failed");
        });
    }

    @Override
    public Call<ApiResponse> changePassword(ChangePasswordRequest request) {
        return new SimpleCall<>(() -> {
            String uid = getCurrentUserId();
            Response<List<User>> checkResp = rawApi.getUserById("eq." + uid).execute();
            if (!checkResp.isSuccessful() || checkResp.body() == null || checkResp.body().isEmpty()) {
                throw new IOException("User not found");
            }
            User u = checkResp.body().get(0);
            if (!u.getPasswordHash().equals(request.getCurrentPassword())) {
                throw new IOException("Current password does not match");
            }

            User update = new User();
            update.setPasswordHash(request.getNewPassword());
            rawApi.updateUser("eq." + uid, update).execute();
            return Response.success(new ApiResponse("Password changed successfully", true));
        });
    }

    @Override
    public Call<ApiResponse> deleteAccount() {
        return new SimpleCall<>(() -> {
            String uid = getCurrentUserId();
            rawApi.deleteUser("eq." + uid).execute();
            SessionManager.getInstance(context).clearSession();
            return Response.success(new ApiResponse("Account deleted successfully", true));
        });
    }

    @Override
    public Call<ExportDataResponse> exportAllData() {
        return new SimpleCall<>(() -> {
            String uid = getCurrentUserId();
            User u = SessionManager.getInstance(context).getUser();
            List<Income> incomes = rawApi.getIncomes("eq." + uid).execute().body();
            List<Expense> expenses = rawApi.getExpenses("eq." + uid).execute().body();
            List<Goal> goals = rawApi.getGoals("eq." + uid).execute().body();
            List<Habit> habits = rawApi.getHabits("eq." + uid).execute().body();
            List<Investment> investments = rawApi.getInvestments("eq." + uid).execute().body();

            return Response.success(new ExportDataResponse(u, incomes, expenses, goals, habits, investments));
        });
    }

    // =========================================================================
    // Income
    // =========================================================================

    @Override
    public Call<List<Income>> getIncomes() {
        return new SimpleCall<>(() -> {
            String uid = getCurrentUserId();
            Response<List<Income>> resp = rawApi.getIncomes("eq." + uid).execute();
            return Response.success(resp.body() != null ? resp.body() : new ArrayList<>());
        });
    }

    @Override
    public Call<Income> addIncome(IncomeRequest request) {
        return new SimpleCall<>(() -> {
            Income inc = new Income();
            inc.setUserId(getCurrentUserId());
            inc.setSource(request.getSource());
            inc.setAmount(request.getAmount());
            inc.setDate(request.getDate());
            inc.setNote(request.getNote());

            Response<List<Income>> resp = rawApi.addIncome(inc).execute();
            if (resp.isSuccessful() && resp.body() != null && !resp.body().isEmpty()) {
                return Response.success(resp.body().get(0));
            }
            String errDetail = "";
            if (resp.errorBody() != null) {
                try {
                    errDetail = " (" + resp.errorBody().string() + ")";
                } catch (Exception ignored) {}
            }
            throw new IOException("Failed to save income entry" + errDetail);
        });
    }

    @Override
    public Call<ApiResponse> deleteIncome(String id) {
        return new SimpleCall<>(() -> {
            rawApi.deleteIncome("eq." + id).execute();
            return Response.success(new ApiResponse("Income deleted", true));
        });
    }

    // =========================================================================
    // Expenses
    // =========================================================================

    @Override
    public Call<List<Expense>> getExpenses(String month) {
        return new SimpleCall<>(() -> {
            String uid = getCurrentUserId();
            Response<List<Expense>> resp;
            if (month != null && !month.trim().isEmpty()) {
                resp = rawApi.getExpensesByMonth("eq." + uid, "like." + month.trim() + "*").execute();
            } else {
                resp = rawApi.getExpenses("eq." + uid).execute();
            }
            return Response.success(resp.body() != null ? resp.body() : new ArrayList<>());
        });
    }

    @Override
    public Call<Expense> addExpense(ExpenseRequest request) {
        return new SimpleCall<>(() -> {
            Expense exp = new Expense();
            exp.setUserId(getCurrentUserId());
            exp.setCategory(request.getCategory());
            exp.setAmount(request.getAmount());
            exp.setDate(request.getDate());
            exp.setNote(request.getNote());

            Response<List<Expense>> resp = rawApi.addExpense(exp).execute();
            if (resp.isSuccessful() && resp.body() != null && !resp.body().isEmpty()) {
                return Response.success(resp.body().get(0));
            }
            String errDetail = "";
            if (resp.errorBody() != null) {
                try {
                    errDetail = " (" + resp.errorBody().string() + ")";
                } catch (Exception ignored) {}
            }
            throw new IOException("Failed to save expense entry" + errDetail);
        });
    }

    @Override
    public Call<ApiResponse> deleteExpense(String id) {
        return new SimpleCall<>(() -> {
            rawApi.deleteExpense("eq." + id).execute();
            return Response.success(new ApiResponse("Expense deleted", true));
        });
    }

    // =========================================================================
    // Habits & Habit Logs
    // =========================================================================

    @Override
    public Call<List<Habit>> getHabits() {
        return new SimpleCall<>(() -> {
            String uid = getCurrentUserId();
            Response<List<Habit>> habResp = rawApi.getHabits("eq." + uid).execute();
            List<Habit> habits = habResp.body() != null ? habResp.body() : new ArrayList<>();

            Response<List<HabitLog>> logResp = rawApi.getHabitLogs("eq." + uid).execute();
            List<HabitLog> logs = logResp.body() != null ? logResp.body() : new ArrayList<>();

            String today = FormatUtils.todayDateString();

            for (Habit h : habits) {
                int count = 0;
                boolean completedToday = false;
                List<String> recent = new ArrayList<>();
                int hid = Integer.parseInt(h.getId());

                for (HabitLog l : logs) {
                    if (l.getHabitId() != null && l.getHabitId() == hid) {
                        count++;
                        recent.add(l.getCompletedOn());
                        if (today.equals(l.getCompletedOn())) {
                            completedToday = true;
                        }
                    }
                }
                h.setTotalCompletions(count);
                h.setCompletedToday(completedToday);
                h.setRecentLogs(recent);
                h.setStreak(completedToday ? count : Math.max(0, count));
            }

            return Response.success(habits);
        });
    }

    @Override
    public Call<Habit> addHabit(HabitRequest request) {
        return new SimpleCall<>(() -> {
            String uid = getCurrentUserId();
            String name = request != null && request.getName() != null ? request.getName().trim() : "Unnamed Habit";
            String freq = request != null && request.getFrequency() != null && !request.getFrequency().trim().isEmpty()
                    ? request.getFrequency().trim()
                    : "daily";
            double target = request != null ? Math.max(0, request.getTargetAmount()) : 0;

            Habit h = new Habit();
            h.setUserId(uid);
            h.setName(name);
            h.setFrequency(freq);
            h.setTargetAmount(target);

            android.util.Log.d("SupabaseService", "Saving habit for user " + uid + ": " + name + " (" + freq + ", target: " + target + ")");

            try {
                Response<List<Habit>> resp = rawApi.addHabit(h).execute();
                if (resp.isSuccessful() && resp.body() != null && !resp.body().isEmpty()) {
                    Habit created = resp.body().get(0);
                    android.util.Log.d("SupabaseService", "Habit saved successfully with id: " + created.getId());
                    return Response.success(created);
                } else {
                    String errorBody = "";
                    if (resp.errorBody() != null) {
                        try {
                            errorBody = resp.errorBody().string();
                        } catch (Exception ignored) {}
                    }
                    String errMessage = "Habit save error: HTTP " + resp.code() + " " + errorBody;
                    android.util.Log.e("SupabaseService", errMessage);
                    System.err.println(errMessage);
                    throw new IOException(errMessage);
                }
            } catch (Exception e) {
                android.util.Log.e("SupabaseService", "Habit save exception: " + e.getMessage(), e);
                System.err.println("Habit save exception: " + e.getMessage());
                if (e instanceof IOException) throw (IOException) e;
                throw new IOException(e.getMessage(), e);
            }
        });
    }

    @Override
    public Call<HabitToggleResponse> completeHabit(String id) {
        return toggleHabit(id);
    }

    @Override
    public Call<HabitToggleResponse> toggleHabit(String id) {
        return new SimpleCall<>(() -> {
            String uid = getCurrentUserId();
            String today = FormatUtils.todayDateString();
            int hid = Integer.parseInt(id);
            int userIdInt = Integer.parseInt(uid);

            Response<List<HabitLog>> logResp = rawApi.getHabitLogs("eq." + uid).execute();
            List<HabitLog> logs = logResp.body() != null ? logResp.body() : new ArrayList<>();
            boolean existsToday = false;

            for (HabitLog l : logs) {
                if (l.getHabitId() != null && l.getHabitId() == hid && today.equals(l.getCompletedOn())) {
                    existsToday = true;
                    break;
                }
            }

            boolean nowCompleted;
            if (existsToday) {
                rawApi.deleteHabitLog("eq." + hid, "eq." + today).execute();
                nowCompleted = false;
            } else {
                rawApi.addHabitLog(new HabitLog(hid, userIdInt, today)).execute();
                nowCompleted = true;
            }

            return Response.success(new HabitToggleResponse(nowCompleted, 1, 1));
        });
    }

    @Override
    public Call<ApiResponse> deleteHabit(String id) {
        return new SimpleCall<>(() -> {
            rawApi.deleteHabit("eq." + id).execute();
            return Response.success(new ApiResponse("Habit deleted", true));
        });
    }

    // =========================================================================
    // Savings Goals
    // =========================================================================

    @Override
    public Call<List<Goal>> getGoals() {
        return new SimpleCall<>(() -> {
            String uid = getCurrentUserId();
            Response<List<Goal>> resp = rawApi.getGoals("eq." + uid).execute();
            List<Goal> goals = resp.body() != null ? resp.body() : new ArrayList<>();
            for (Goal g : goals) {
                int pct = g.getTargetAmount() > 0 ? (int) Math.min(100, Math.round((g.getSavedAmount() / g.getTargetAmount()) * 100)) : 0;
                g.setProgressPercent(pct);
            }
            return Response.success(goals);
        });
    }

    @Override
    public Call<Goal> addGoal(GoalRequest request) {
        return new SimpleCall<>(() -> {
            String uid = getCurrentUserId();
            String title = request != null && request.getTitle() != null ? request.getTitle().trim() : "Untitled Goal";
            double target = request != null ? Math.max(0, request.getTargetAmount()) : 0;
            double saved = request != null ? Math.max(0, request.getSavedAmount()) : 0;
            String deadline = request != null && request.getDeadline() != null && !request.getDeadline().trim().isEmpty()
                    ? request.getDeadline().trim()
                    : null;

            Goal g = new Goal();
            g.setUserId(uid);
            g.setTitle(title);
            g.setTargetAmount(target);
            g.setSavedAmount(saved);
            g.setDeadline(deadline);

            android.util.Log.d("SupabaseService", "Saving goal for user " + uid + ": " + title + " (target: " + target + ", saved: " + saved + ")");

            try {
                Response<List<Goal>> resp = rawApi.addGoal(g).execute();
                if (resp.isSuccessful() && resp.body() != null && !resp.body().isEmpty()) {
                    Goal created = resp.body().get(0);
                    int pct = created.getTargetAmount() > 0 ? (int) Math.min(100, Math.round((created.getSavedAmount() / created.getTargetAmount()) * 100)) : 0;
                    created.setProgressPercent(pct);
                    android.util.Log.d("SupabaseService", "Goal saved successfully with id: " + created.getId());
                    return Response.success(created);
                } else {
                    String err = resp.errorBody() != null ? resp.errorBody().string() : "";
                    String errMessage = "Goal save error: HTTP " + resp.code() + " " + err;
                    android.util.Log.e("SupabaseService", errMessage);
                    System.err.println(errMessage);
                    throw new IOException(errMessage);
                }
            } catch (Exception e) {
                android.util.Log.e("SupabaseService", "Goal save exception: " + e.getMessage(), e);
                System.err.println("Goal save exception: " + e.getMessage());
                if (e instanceof IOException) throw (IOException) e;
                throw new IOException(e.getMessage(), e);
            }
        });
    }

    @Override
    public Call<Goal> contributeGoal(String id, GoalContributeRequest request) {
        return new SimpleCall<>(() -> {
            String uid = getCurrentUserId();
            Response<List<Goal>> listResp = rawApi.getGoals("eq." + uid).execute();
            Goal target = null;
            if (listResp.body() != null) {
                for (Goal g : listResp.body()) {
                    if (id.equals(g.getId())) {
                        target = g;
                        break;
                    }
                }
            }
            if (target == null) throw new IOException("Goal not found");

            double newSaved = target.getSavedAmount() + request.getAmount();
            Map<String, Object> updates = new HashMap<>();
            updates.put("saved_amount", newSaved);

            try {
                Response<List<Goal>> resp = rawApi.updateGoalFields("eq." + id, updates).execute();
                if (resp.isSuccessful() && resp.body() != null && !resp.body().isEmpty()) {
                    Goal updated = resp.body().get(0);
                    if (updated.getTargetAmount() <= 0 && target.getTargetAmount() > 0) {
                        updated.setTargetAmount(target.getTargetAmount());
                    }
                    int pct = updated.getTargetAmount() > 0 ? (int) Math.min(100, Math.round((updated.getSavedAmount() / updated.getTargetAmount()) * 100)) : 0;
                    updated.setProgressPercent(pct);
                    return Response.success(updated);
                } else {
                    String err = resp.errorBody() != null ? resp.errorBody().string() : "";
                    String errMessage = "Goal contribute error: HTTP " + resp.code() + " " + err;
                    android.util.Log.e("SupabaseService", errMessage);
                    System.err.println(errMessage);
                    throw new IOException(errMessage);
                }
            } catch (Exception e) {
                android.util.Log.e("SupabaseService", "Goal contribute exception: " + e.getMessage(), e);
                System.err.println("Goal contribute exception: " + e.getMessage());
                if (e instanceof IOException) throw (IOException) e;
                throw new IOException(e.getMessage(), e);
            }
        });
    }

    @Override
    public Call<ApiResponse> deleteGoal(String id) {
        return new SimpleCall<>(() -> {
            rawApi.deleteGoal("eq." + id).execute();
            return Response.success(new ApiResponse("Goal deleted", true));
        });
    }

    // =========================================================================
    // Investments
    // =========================================================================

    @Override
    public Call<List<Investment>> getInvestments() {
        return new SimpleCall<>(() -> {
            String uid = getCurrentUserId();
            Response<List<Investment>> resp = rawApi.getInvestments("eq." + uid).execute();
            return Response.success(resp.body() != null ? resp.body() : new ArrayList<>());
        });
    }

    @Override
    public Call<Investment> addInvestment(InvestmentRequest request) {
        return new SimpleCall<>(() -> {
            Investment inv = new Investment();
            inv.setUserId(getCurrentUserId());
            inv.setAssetName(request.getAssetName());
            inv.setAssetType(request.getAssetType());
            inv.setAmountInvested(request.getAmountInvested());
            inv.setCurrentValue(request.getCurrentValue());
            inv.setDate(request.getDate());

            Response<List<Investment>> resp = rawApi.addInvestment(inv).execute();
            if (resp.isSuccessful() && resp.body() != null && !resp.body().isEmpty()) {
                return Response.success(resp.body().get(0));
            }
            String errDetail = "";
            if (resp.errorBody() != null) {
                try {
                    errDetail = " (" + resp.errorBody().string() + ")";
                } catch (Exception ignored) {}
            }
            throw new IOException("Failed to save investment" + errDetail);
        });
    }

    @Override
    public Call<Investment> updateInvestmentValue(String id, InvestmentUpdateRequest request) {
        return new SimpleCall<>(() -> {
            Investment update = new Investment();
            update.setCurrentValue(request.getCurrentValue());

            Response<List<Investment>> resp = rawApi.updateInvestment("eq." + id, update).execute();
            if (resp.isSuccessful() && resp.body() != null && !resp.body().isEmpty()) {
                return Response.success(resp.body().get(0));
            }
            throw new IOException("Failed to update investment valuation");
        });
    }

    @Override
    public Call<ApiResponse> deleteInvestment(String id) {
        return new SimpleCall<>(() -> {
            rawApi.deleteInvestment("eq." + id).execute();
            return Response.success(new ApiResponse("Investment deleted", true));
        });
    }

    // =========================================================================
    // Analytics Calculation (Computed directly from Supabase tables)
    // =========================================================================

    @Override
    public Call<AnalyticsSummary> getAnalyticsSummary() {
        return new SimpleCall<>(() -> {
            String uid = getCurrentUserId();

            List<Income> incomes = rawApi.getIncomes("eq." + uid).execute().body();
            List<Expense> expenses = rawApi.getExpenses("eq." + uid).execute().body();
            List<Goal> goals = rawApi.getGoals("eq." + uid).execute().body();
            List<Investment> investments = rawApi.getInvestments("eq." + uid).execute().body();
            List<Habit> habits = rawApi.getHabits("eq." + uid).execute().body();
            List<HabitLog> logs = rawApi.getHabitLogs("eq." + uid).execute().body();

            double totalInc = 0;
            if (incomes != null) {
                for (Income i : incomes) totalInc += i.getAmount();
            }

            double totalExp = 0;
            Map<String, Double> catMap = new HashMap<>();
            if (expenses != null) {
                for (Expense e : expenses) {
                    totalExp += e.getAmount();
                    catMap.put(e.getCategory(), catMap.getOrDefault(e.getCategory(), 0.0) + e.getAmount());
                }
            }

            double totalInvested = 0;
            double currentInvestVal = 0;
            if (investments != null) {
                for (Investment inv : investments) {
                    totalInvested += inv.getAmountInvested();
                    currentInvestVal += inv.getCurrentValue();
                }
            }

            double savedInGoals = 0;
            if (goals != null) {
                for (Goal g : goals) savedInGoals += g.getSavedAmount();
            }

            double netCash = totalInc - totalExp;
            double netWorth = netCash + currentInvestVal;

            // Monthly breakdown (last 6 months)
            List<MonthlyData> monthly = new ArrayList<>();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM", Locale.US);
            Calendar cal = Calendar.getInstance();
            for (int i = 5; i >= 0; i--) {
                Calendar c = (Calendar) cal.clone();
                c.add(Calendar.MONTH, -i);
                String monthKey = sdf.format(c.getTime());
                String monthLabel = new SimpleDateFormat("MMM", Locale.US).format(c.getTime());

                double mInc = 0;
                if (incomes != null) {
                    for (Income in : incomes) {
                        if (in.getDate() != null && in.getDate().startsWith(monthKey)) mInc += in.getAmount();
                    }
                }
                double mExp = 0;
                if (expenses != null) {
                    for (Expense ex : expenses) {
                        if (ex.getDate() != null && ex.getDate().startsWith(monthKey)) mExp += ex.getAmount();
                    }
                }
                monthly.add(new MonthlyData(monthLabel, mInc, mExp));
            }

            // Category breakdown
            List<CategoryData> catList = new ArrayList<>();
            for (Map.Entry<String, Double> entry : catMap.entrySet()) {
                double pct = totalExp > 0 ? (entry.getValue() / totalExp) * 100.0 : 0;
                catList.add(new CategoryData(entry.getKey(), entry.getValue(), Math.round(pct * 10.0) / 10.0));
            }

            String today = FormatUtils.todayDateString();
            int completedToday = 0;
            if (logs != null) {
                for (HabitLog l : logs) {
                    if (today.equals(l.getCompletedOn())) completedToday++;
                }
            }

            AnalyticsSummary summary = new AnalyticsSummary();
            setField(summary, "totalIncome", totalInc);
            setField(summary, "totalExpense", totalExp);
            setField(summary, "netCashSavings", netCash);
            setField(summary, "totalInvested", totalInvested);
            setField(summary, "investmentsCurrentValue", currentInvestVal);
            setField(summary, "totalSavedInGoals", savedInGoals);
            setField(summary, "netWorth", netWorth);
            setField(summary, "monthly", monthly);
            setField(summary, "categoryBreakdown", catList);
            setField(summary, "habitCount", habits != null ? habits.size() : 0);
            setField(summary, "habitsCompletedToday", completedToday);

            return Response.success(summary);
        });
    }

    private void setField(Object obj, String fieldName, Object val) {
        try {
            java.lang.reflect.Field f = obj.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(obj, val);
        } catch (Exception ignored) {}
    }

    // =========================================================================
    // Feedback
    // =========================================================================

    @Override
    public Call<ApiResponse> submitFeedback(FeedbackRequest request) {
        return new SimpleCall<>(() -> {
            FeedbackItem item = new FeedbackItem();
            item.setUserId(getCurrentUserId());
            item.setMessage(request.getMessage());
            item.setStatus("open");

            rawApi.submitFeedback(item).execute();
            return Response.success(new ApiResponse("Feedback submitted successfully", true));
        });
    }

    // =========================================================================
    // Admin Operations
    // =========================================================================

    @Override
    public Call<AdminStats> getAdminStats() {
        return new SimpleCall<>(() -> {
            List<User> users = rawApi.getAllUsers().execute().body();
            List<FeedbackItem> feedback = rawApi.getAllFeedback().execute().body();

            int totalUsers = users != null ? users.size() : 0;
            int totalFeed = feedback != null ? feedback.size() : 0;
            int openFeed = 0;
            if (feedback != null) {
                for (FeedbackItem f : feedback) {
                    if ("open".equalsIgnoreCase(f.getStatus())) openFeed++;
                }
            }

            AdminStats stats = new AdminStats();
            setField(stats, "totalUsers", totalUsers);
            setField(stats, "totalFeedback", totalFeed);
            setField(stats, "openFeedback", openFeed);
            return Response.success(stats);
        });
    }

    @Override
    public Call<List<AdminUserItem>> getAdminUsers() {
        return new SimpleCall<>(() -> {
            List<User> users = rawApi.getAllUsers().execute().body();
            List<AdminUserItem> list = new ArrayList<>();
            if (users != null) {
                for (User u : users) {
                    list.add(new AdminUserItem(u.getId(), u.getName(), u.getEmail(), u.getRole(), u.getCreatedAt()));
                }
            }
            return Response.success(list);
        });
    }

    @Override
    public Call<ApiResponse> updateUserRole(String id, RoleUpdateRequest request) {
        return new SimpleCall<>(() -> {
            User update = new User();
            update.setRole(request.getRole());
            rawApi.updateUser("eq." + id, update).execute();
            return Response.success(new ApiResponse("Role updated", true));
        });
    }

    @Override
    public Call<ApiResponse> deleteAdminUser(String id) {
        return new SimpleCall<>(() -> {
            rawApi.deleteUser("eq." + id).execute();
            return Response.success(new ApiResponse("User deleted", true));
        });
    }

    @Override
    public Call<List<FeedbackItem>> getAdminFeedback() {
        return new SimpleCall<>(() -> {
            Response<List<FeedbackItem>> resp = rawApi.getAllFeedback().execute();
            return Response.success(resp.body() != null ? resp.body() : new ArrayList<>());
        });
    }

    @Override
    public Call<ApiResponse> updateFeedbackStatus(String id, FeedbackStatusRequest request) {
        return new SimpleCall<>(() -> {
            FeedbackItem item = new FeedbackItem();
            item.setStatus(request.getStatus());
            rawApi.updateFeedback("eq." + id, item).execute();
            return Response.success(new ApiResponse("Status updated", true));
        });
    }

    @Override
    public Call<ApiResponse> checkHealth() {
        return new SimpleCall<>(() -> {
            Response<List<User>> resp = rawApi.getAllUsers().execute();
            if (resp.isSuccessful()) {
                return Response.success(new ApiResponse("ok", true));
            }
            throw new IOException("Supabase connection check failed: HTTP " + resp.code());
        });
    }
}
