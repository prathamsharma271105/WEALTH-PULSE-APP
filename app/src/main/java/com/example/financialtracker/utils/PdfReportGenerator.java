package com.example.financialtracker.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import androidx.core.content.FileProvider;
import com.example.financialtracker.models.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class PdfReportGenerator {

    public static File generateReport(Context context, User user, AnalyticsSummary summary,
                                      List<Income> incomes, List<Expense> expenses,
                                      List<Goal> goals, List<Habit> habits,
                                      List<Investment> investments) throws IOException {

        PdfDocument document = new PdfDocument();
        int pageWidth = 595; // A4 standard width (pt)
        int pageHeight = 842; // A4 standard height (pt)

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        Paint titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        titlePaint.setColor(Color.parseColor("#1E3A8A"));
        titlePaint.setTextSize(20f);
        titlePaint.setFakeBoldText(true);

        Paint headerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        headerPaint.setColor(Color.parseColor("#0F172A"));
        headerPaint.setTextSize(14f);
        headerPaint.setFakeBoldText(true);

        Paint bodyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bodyPaint.setColor(Color.parseColor("#334155"));
        bodyPaint.setTextSize(10f);

        Paint subPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        subPaint.setColor(Color.parseColor("#64748B"));
        subPaint.setTextSize(9f);

        Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(Color.parseColor("#CBD5E1"));
        linePaint.setStrokeWidth(1f);

        Paint boxPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        boxPaint.setColor(Color.parseColor("#F1F5F9"));

        String currency = user != null ? user.getCurrency() : "INR";

        // Top Banner / Header
        canvas.drawRect(0, 0, pageWidth, 75, boxPaint);
        canvas.drawText("WealthPulse – Financial Intelligence Report", 30, 38, titlePaint);
        canvas.drawText("Generated on " + FormatUtils.todayDateString() + " | Account: " + (user != null ? user.getName() : "User") + " (" + (user != null ? user.getEmail() : "") + ")", 30, 58, subPaint);

        int y = 100;

        // Executive Financial Summary Box
        canvas.drawText("Executive Financial Position", 30, y, headerPaint);
        y += 10;
        canvas.drawLine(30, y, pageWidth - 30, y, linePaint);
        y += 20;

        if (summary != null) {
            String netWorthStr = FormatUtils.formatCurrency(summary.getNetWorth(), currency);
            String incStr = FormatUtils.formatCurrency(summary.getTotalIncome(), currency);
            String expStr = FormatUtils.formatCurrency(summary.getTotalExpense(), currency);
            String savStr = FormatUtils.formatCurrency(summary.getNetCashSavings(), currency);
            String invStr = FormatUtils.formatCurrency(summary.getInvestmentsCurrentValue(), currency);
            String goalStr = FormatUtils.formatCurrency(summary.getTotalSavedInGoals(), currency);

            canvas.drawText("Net Worth: " + netWorthStr, 35, y, bodyPaint);
            canvas.drawText("Total Inflow: " + incStr, 220, y, bodyPaint);
            canvas.drawText("Total Outflow: " + expStr, 400, y, bodyPaint);
            y += 18;

            canvas.drawText("Cash Savings: " + savStr, 35, y, bodyPaint);
            canvas.drawText("Portfolio Value: " + invStr, 220, y, bodyPaint);
            canvas.drawText("Goals Saved: " + goalStr, 400, y, bodyPaint);
            y += 28;
        }

        // Recent Incomes
        canvas.drawText("Recent Incomes (Inflows)", 30, y, headerPaint);
        y += 10;
        canvas.drawLine(30, y, pageWidth - 30, y, linePaint);
        y += 18;

        if (incomes != null && !incomes.isEmpty()) {
            int maxInc = Math.min(incomes.size(), 4);
            for (int i = 0; i < maxInc; i++) {
                Income inc = incomes.get(i);
                canvas.drawText("• " + inc.getSource() + " (" + FormatUtils.formatDate(inc.getDate()) + ")", 35, y, bodyPaint);
                canvas.drawText("+" + FormatUtils.formatCurrency(inc.getAmount(), currency), 450, y, bodyPaint);
                y += 15;
            }
        } else {
            canvas.drawText("No income records recorded.", 35, y, subPaint);
            y += 15;
        }
        y += 12;

        // Recent Expenses
        canvas.drawText("Recent Expenses (Outflows)", 30, y, headerPaint);
        y += 10;
        canvas.drawLine(30, y, pageWidth - 30, y, linePaint);
        y += 18;

        if (expenses != null && !expenses.isEmpty()) {
            int maxExp = Math.min(expenses.size(), 4);
            for (int i = 0; i < maxExp; i++) {
                Expense exp = expenses.get(i);
                canvas.drawText("• " + exp.getCategory() + " - " + exp.getNote() + " (" + FormatUtils.formatDate(exp.getDate()) + ")", 35, y, bodyPaint);
                canvas.drawText("-" + FormatUtils.formatCurrency(exp.getAmount(), currency), 450, y, bodyPaint);
                y += 15;
            }
        } else {
            canvas.drawText("No expense records recorded.", 35, y, subPaint);
            y += 15;
        }
        y += 12;

        // Savings Goals
        canvas.drawText("Savings Goals & Milestones", 30, y, headerPaint);
        y += 10;
        canvas.drawLine(30, y, pageWidth - 30, y, linePaint);
        y += 18;

        if (goals != null && !goals.isEmpty()) {
            int maxGoals = Math.min(goals.size(), 3);
            for (int i = 0; i < maxGoals; i++) {
                Goal g = goals.get(i);
                canvas.drawText("• " + g.getTitle() + " [Progress: " + g.getProgressPercent() + "%]", 35, y, bodyPaint);
                canvas.drawText(FormatUtils.formatCurrency(g.getSavedAmount(), currency) + " / " + FormatUtils.formatCurrency(g.getTargetAmount(), currency), 400, y, bodyPaint);
                y += 15;
            }
        } else {
            canvas.drawText("No active savings goals.", 35, y, subPaint);
            y += 15;
        }
        y += 12;

        // Habit Streaks
        canvas.drawText("Daily Financial Habits & Streaks", 30, y, headerPaint);
        y += 10;
        canvas.drawLine(30, y, pageWidth - 30, y, linePaint);
        y += 18;

        if (habits != null && !habits.isEmpty()) {
            int maxHabits = Math.min(habits.size(), 3);
            for (int i = 0; i < maxHabits; i++) {
                Habit h = habits.get(i);
                canvas.drawText("• " + h.getName() + " (" + h.getFrequency() + ")", 35, y, bodyPaint);
                canvas.drawText("Streak: " + h.getStreak() + " days | Completed Today: " + (h.isCompletedToday() ? "Yes" : "No"), 350, y, bodyPaint);
                y += 15;
            }
        } else {
            canvas.drawText("No active financial habits.", 35, y, subPaint);
            y += 15;
        }
        y += 25;

        // Footer
        canvas.drawLine(30, pageHeight - 40, pageWidth - 30, pageHeight - 40, linePaint);
        canvas.drawText("WealthPulse Personal Finance Platform • Confidential Client Statement", 30, pageHeight - 25, subPaint);

        document.finishPage(page);

        // Write to file
        File reportsDir = new File(context.getExternalFilesDir(null), "reports");
        if (!reportsDir.exists()) reportsDir.mkdirs();
        File pdfFile = new File(reportsDir, "WealthPulse_Statement_" + System.currentTimeMillis() + ".pdf");
        FileOutputStream fos = new FileOutputStream(pdfFile);
        document.writeTo(fos);
        document.close();
        fos.close();

        return pdfFile;
    }

    public static void openPdfFile(Context context, File pdfFile) {
        if (pdfFile == null || !pdfFile.exists()) return;
        Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", pdfFile);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/pdf");
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_GRANT_READ_URI_PERMISSION);

        Intent chooser = Intent.createChooser(intent, "Open WealthPulse Financial Statement");
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(chooser);
    }

    public static void sharePdfFile(Context context, File pdfFile) {
        if (pdfFile == null || !pdfFile.exists()) return;
        Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", pdfFile);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        Intent chooser = Intent.createChooser(intent, "Share WealthPulse Financial Statement");
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(chooser);
    }
}
