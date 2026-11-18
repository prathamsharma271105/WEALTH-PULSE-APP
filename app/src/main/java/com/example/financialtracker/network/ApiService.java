package com.example.financialtracker.network;

import com.example.financialtracker.models.*;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {

    // --- Authentication & Profile ---
    @POST("api/auth/register")
    Call<AuthResponse> register(@Body RegisterRequest request);

    @POST("api/auth/login")
    Call<AuthResponse> login(@Body LoginRequest request);

    @GET("api/auth/me")
    Call<AuthResponse> getProfile();

    @PATCH("api/auth/profile")
    Call<AuthResponse> updateProfile(@Body UpdateProfileRequest request);

    @POST("api/auth/change-password")
    Call<ApiResponse> changePassword(@Body ChangePasswordRequest request);

    @DELETE("api/auth/delete-account")
    Call<ApiResponse> deleteAccount();

    @GET("api/auth/export-all")
    Call<ExportDataResponse> exportAllData();

    // --- Income ---
    @GET("api/income")
    Call<List<Income>> getIncomes();

    @POST("api/income")
    Call<Income> addIncome(@Body IncomeRequest request);

    @DELETE("api/income/{id}")
    Call<ApiResponse> deleteIncome(@Path("id") String id);

    // --- Expenses ---
    @GET("api/expenses")
    Call<List<Expense>> getExpenses(@Query("month") String month);

    @POST("api/expenses")
    Call<Expense> addExpense(@Body ExpenseRequest request);

    @DELETE("api/expenses/{id}")
    Call<ApiResponse> deleteExpense(@Path("id") String id);

    // --- Habits ---
    @GET("api/habits")
    Call<List<Habit>> getHabits();

    @POST("api/habits")
    Call<Habit> addHabit(@Body HabitRequest request);

    @POST("api/habits/{id}/complete")
    Call<HabitToggleResponse> completeHabit(@Path("id") String id);

    @POST("api/habits/{id}/toggle")
    Call<HabitToggleResponse> toggleHabit(@Path("id") String id);

    @DELETE("api/habits/{id}")
    Call<ApiResponse> deleteHabit(@Path("id") String id);

    // --- Savings Goals ---
    @GET("api/goals")
    Call<List<Goal>> getGoals();

    @POST("api/goals")
    Call<Goal> addGoal(@Body GoalRequest request);

    @PATCH("api/goals/{id}/contribute")
    Call<Goal> contributeGoal(@Path("id") String id, @Body GoalContributeRequest request);

    @DELETE("api/goals/{id}")
    Call<ApiResponse> deleteGoal(@Path("id") String id);

    // --- Investments ---
    @GET("api/investments")
    Call<List<Investment>> getInvestments();

    @POST("api/investments")
    Call<Investment> addInvestment(@Body InvestmentRequest request);

    @PATCH("api/investments/{id}")
    Call<Investment> updateInvestmentValue(@Path("id") String id, @Body InvestmentUpdateRequest request);

    @DELETE("api/investments/{id}")
    Call<ApiResponse> deleteInvestment(@Path("id") String id);

    // --- Analytics ---
    @GET("api/analytics/summary")
    Call<AnalyticsSummary> getAnalyticsSummary();

    // --- Feedback ---
    @POST("api/feedback")
    Call<ApiResponse> submitFeedback(@Body FeedbackRequest request);

    // --- Admin ---
    @GET("api/admin/stats")
    Call<AdminStats> getAdminStats();

    @GET("api/admin/users")
    Call<List<AdminUserItem>> getAdminUsers();

    @PATCH("api/admin/users/{id}/role")
    Call<ApiResponse> updateUserRole(@Path("id") String id, @Body RoleUpdateRequest request);

    @DELETE("api/admin/users/{id}")
    Call<ApiResponse> deleteAdminUser(@Path("id") String id);

    @GET("api/admin/feedback")
    Call<List<FeedbackItem>> getAdminFeedback();

    @PATCH("api/admin/feedback/{id}")
    Call<ApiResponse> updateFeedbackStatus(@Path("id") String id, @Body FeedbackStatusRequest request);

    // --- Health ---
    @GET("api/health")
    Call<ApiResponse> checkHealth();
}
