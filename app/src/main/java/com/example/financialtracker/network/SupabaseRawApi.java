package com.example.financialtracker.network;

import com.example.financialtracker.models.*;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.*;

public interface SupabaseRawApi {

    // --- Users Table ---
    @GET("users")
    Call<List<User>> getUserByEmail(@Query("email") String emailFilter);

    @GET("users")
    Call<List<User>> getUserById(@Query("id") String idFilter);

    @GET("users?order=created_at.desc")
    Call<List<User>> getAllUsers();

    @POST("users")
    Call<List<User>> createUser(@Body User user);

    @PATCH("users")
    Call<List<User>> updateUser(@Query("id") String idFilter, @Body User user);

    @DELETE("users")
    Call<Void> deleteUser(@Query("id") String idFilter);

    // --- Income Table ---
    @GET("income?order=date.desc")
    Call<List<Income>> getIncomes(@Query("user_id") String userIdFilter);

    @POST("income")
    Call<List<Income>> addIncome(@Body Income income);

    @DELETE("income")
    Call<Void> deleteIncome(@Query("id") String idFilter);

    // --- Expenses Table ---
    @GET("expenses?order=date.desc")
    Call<List<Expense>> getExpenses(@Query("user_id") String userIdFilter);

    @GET("expenses?order=date.desc")
    Call<List<Expense>> getExpensesByMonth(@Query("user_id") String userIdFilter, @Query("date") String dateFilter);

    @POST("expenses")
    Call<List<Expense>> addExpense(@Body Expense expense);

    @DELETE("expenses")
    Call<Void> deleteExpense(@Query("id") String idFilter);

    // --- Goals Table ---
    @GET("goals?order=created_at.desc")
    Call<List<Goal>> getGoals(@Query("user_id") String userIdFilter);

    @POST("goals")
    Call<List<Goal>> addGoal(@Body Goal goal);

    @PATCH("goals")
    Call<List<Goal>> updateGoal(@Query("id") String idFilter, @Body Goal goal);

    @PATCH("goals")
    Call<List<Goal>> updateGoalFields(@Query("id") String idFilter, @Body Map<String, Object> updates);

    @DELETE("goals")
    Call<Void> deleteGoal(@Query("id") String idFilter);

    // --- Habits Table ---
    @GET("habits")
    Call<List<Habit>> getHabits(@Query("user_id") String userIdFilter);

    @POST("habits")
    Call<List<Habit>> addHabit(@Body Habit habit);

    @DELETE("habits")
    Call<Void> deleteHabit(@Query("id") String idFilter);

    // --- Habit Logs Table ---
    @GET("habit_logs")
    Call<List<HabitLog>> getHabitLogs(@Query("user_id") String userIdFilter);

    @POST("habit_logs")
    Call<List<HabitLog>> addHabitLog(@Body HabitLog log);

    @DELETE("habit_logs")
    Call<Void> deleteHabitLog(@Query("habit_id") String habitIdFilter, @Query("completed_on") String dateFilter);

    // --- Investments Table ---
    @GET("investments?order=created_at.desc")
    Call<List<Investment>> getInvestments(@Query("user_id") String userIdFilter);

    @POST("investments")
    Call<List<Investment>> addInvestment(@Body Investment investment);

    @PATCH("investments")
    Call<List<Investment>> updateInvestment(@Query("id") String idFilter, @Body Investment investment);

    @DELETE("investments")
    Call<Void> deleteInvestment(@Query("id") String idFilter);

    // --- Feedback Table ---
    @POST("feedback")
    Call<List<FeedbackItem>> submitFeedback(@Body FeedbackItem item);

    @GET("feedback?order=created_at.desc")
    Call<List<FeedbackItem>> getAllFeedback();

    @PATCH("feedback")
    Call<List<FeedbackItem>> updateFeedback(@Query("id") String idFilter, @Body FeedbackItem item);
}
