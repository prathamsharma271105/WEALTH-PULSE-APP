package com.example.financialtracker.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;
import com.example.financialtracker.models.CategoryData;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class DonutChartView extends View {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint subTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF oval = new RectF();

    private final List<CategoryData> dataList = new ArrayList<>();
    private double totalAmount = 0;
    private String centerTitle = "Total Expenses";
    private String centerValue = "₹0.00";

    private final int[] sliceColors = new int[]{
            Color.parseColor("#3B82F6"), // Blue
            Color.parseColor("#10B981"), // Emerald
            Color.parseColor("#F59E0B"), // Amber
            Color.parseColor("#F43F5E"), // Crimson
            Color.parseColor("#8B5CF6"), // Purple
            Color.parseColor("#0EA5E9"), // Sky
            Color.parseColor("#EC4899"), // Pink
            Color.parseColor("#14B8A6"), // Teal
            Color.parseColor("#6366F1")  // Indigo
    };

    public DonutChartView(Context context) {
        super(context);
        init();
    }

    public DonutChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DonutChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);

        textPaint.setColor(Color.WHITE);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);

        subTextPaint.setColor(Color.parseColor("#9CA3AF"));
        subTextPaint.setTextAlign(Paint.Align.CENTER);
    }

    public void setData(List<CategoryData> categories, String formattedTotal, boolean isDarkTheme) {
        this.dataList.clear();
        this.totalAmount = 0;

        if (categories != null) {
            this.dataList.addAll(categories);
            for (CategoryData c : categories) {
                totalAmount += c.getTotal();
            }
        }

        this.centerValue = formattedTotal;
        textPaint.setColor(isDarkTheme ? Color.WHITE : Color.parseColor("#0F172A"));
        subTextPaint.setColor(isDarkTheme ? Color.parseColor("#9CA3AF") : Color.parseColor("#64748B"));

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        if (width == 0 || height == 0) return;

        int size = Math.min(width, height);
        float strokeWidth = size * 0.12f;
        paint.setStrokeWidth(strokeWidth);

        float padding = strokeWidth * 0.8f;
        oval.set(
                (width - size) / 2f + padding,
                (height - size) / 2f + padding,
                (width + size) / 2f - padding,
                (height + size) / 2f - padding
        );

        if (dataList.isEmpty() || totalAmount <= 0) {
            paint.setColor(Color.parseColor("#334155"));
            canvas.drawArc(oval, 0, 360, false, paint);

            textPaint.setTextSize(size * 0.08f);
            canvas.drawText("No Data", width / 2f, height / 2f + (textPaint.getTextSize() / 3f), textPaint);
            return;
        }

        float startAngle = -90f;
        for (int i = 0; i < dataList.size(); i++) {
            CategoryData item = dataList.get(i);
            float sweepAngle = (float) ((item.getTotal() / totalAmount) * 360f);

            // Small gap between slices
            float drawSweep = Math.max(sweepAngle - 3f, 2f);
            paint.setColor(sliceColors[i % sliceColors.length]);

            canvas.drawArc(oval, startAngle, drawSweep, false, paint);
            startAngle += sweepAngle;
        }

        // Center text
        subTextPaint.setTextSize(size * 0.065f);
        canvas.drawText(centerTitle, width / 2f, (height / 2f) - (subTextPaint.getTextSize() * 0.6f), subTextPaint);

        textPaint.setTextSize(size * 0.09f);
        canvas.drawText(centerValue, width / 2f, (height / 2f) + (textPaint.getTextSize() * 0.8f), textPaint);
    }
}
