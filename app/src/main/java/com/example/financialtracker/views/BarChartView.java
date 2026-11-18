package com.example.financialtracker.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;
import com.example.financialtracker.models.MonthlyData;
import java.util.ArrayList;
import java.util.List;

public class BarChartView extends View {

    private final Paint incomePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint expensePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF barRect = new RectF();

    private final List<MonthlyData> dataList = new ArrayList<>();
    private double maxVal = 1000;

    public BarChartView(Context context) {
        super(context);
        init();
    }

    public BarChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BarChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        incomePaint.setColor(Color.parseColor("#10B981")); // Emerald
        expensePaint.setColor(Color.parseColor("#F43F5E")); // Rose

        gridPaint.setColor(Color.parseColor("#1FFFFFFF"));
        gridPaint.setStrokeWidth(2f);

        labelPaint.setColor(Color.parseColor("#9CA3AF"));
        labelPaint.setTextAlign(Paint.Align.CENTER);
        labelPaint.setTextSize(26f);
    }

    public void setData(List<MonthlyData> monthlyData, boolean isDarkTheme) {
        this.dataList.clear();
        this.maxVal = 1000;

        if (monthlyData != null) {
            this.dataList.addAll(monthlyData);
            for (MonthlyData m : monthlyData) {
                if (m.getIncome() > maxVal) maxVal = m.getIncome();
                if (m.getExpense() > maxVal) maxVal = m.getExpense();
            }
        }

        gridPaint.setColor(isDarkTheme ? Color.parseColor("#1FFFFFFF") : Color.parseColor("#1F000000"));
        labelPaint.setColor(isDarkTheme ? Color.parseColor("#9CA3AF") : Color.parseColor("#64748B"));

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        if (width == 0 || height == 0) return;

        float bottomPadding = 50f;
        float topPadding = 30f;
        float chartHeight = height - bottomPadding - topPadding;

        // Draw 3 subtle horizontal gridlines
        for (int i = 0; i <= 3; i++) {
            float y = topPadding + (chartHeight * (i / 3f));
            canvas.drawLine(40f, y, width - 40f, y, gridPaint);
        }

        if (dataList.isEmpty()) {
            labelPaint.setTextSize(32f);
            canvas.drawText("No monthly transaction history yet", width / 2f, height / 2f, labelPaint);
            return;
        }

        int count = dataList.size();
        float sectionWidth = (width - 80f) / count;
        float barWidth = Math.min(sectionWidth * 0.35f, 40f);

        for (int i = 0; i < count; i++) {
            MonthlyData item = dataList.get(i);
            float sectionCenterX = 40f + (i * sectionWidth) + (sectionWidth / 2f);

            float incH = (float) ((item.getIncome() / maxVal) * chartHeight);
            float expH = (float) ((item.getExpense() / maxVal) * chartHeight);

            // Income bar (left)
            float incLeft = sectionCenterX - barWidth - 4f;
            float incTop = height - bottomPadding - incH;
            barRect.set(incLeft, incTop, incLeft + barWidth, height - bottomPadding);
            canvas.drawRoundRect(barRect, 8f, 8f, incomePaint);

            // Expense bar (right)
            float expLeft = sectionCenterX + 4f;
            float expTop = height - bottomPadding - expH;
            barRect.set(expLeft, expTop, expLeft + barWidth, height - bottomPadding);
            canvas.drawRoundRect(barRect, 8f, 8f, expensePaint);

            // Month Label
            String label = item.getMonth();
            if (label.length() >= 7) {
                label = label.substring(5); // e.g., "10" from "2026-10"
            }
            labelPaint.setTextSize(26f);
            canvas.drawText(label, sectionCenterX, height - 12f, labelPaint);
        }
    }
}
