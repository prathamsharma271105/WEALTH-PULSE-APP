package com.example.financialtracker.utils;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FormatUtils {

    public static String formatCurrency(double amount, String currencyCode) {
        if (currencyCode == null || currencyCode.trim().isEmpty()) {
            currencyCode = "INR";
        }
        currencyCode = currencyCode.trim().toUpperCase();

        String symbol;
        switch (currencyCode) {
            case "INR":
                symbol = "₹";
                break;
            case "USD":
                symbol = "$";
                break;
            case "EUR":
                symbol = "€";
                break;
            case "GBP":
                symbol = "£";
                break;
            case "AED":
                symbol = "AED ";
                break;
            case "CAD":
                symbol = "C$";
                break;
            case "AUD":
                symbol = "A$";
                break;
            case "SGD":
                symbol = "S$";
                break;
            case "JPY":
                symbol = "¥";
                break;
            default:
                symbol = currencyCode + " ";
                break;
        }

        DecimalFormat df = new DecimalFormat("#,##0.00");
        return symbol + df.format(amount);
    }

    public static String formatDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return "";
        try {
            SimpleDateFormat inFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = inFormat.parse(dateStr.trim());
            if (date != null) {
                SimpleDateFormat outFormat = new SimpleDateFormat("dd MMM, yyyy", Locale.getDefault());
                return outFormat.format(date);
            }
        } catch (ParseException ignored) {
            // Might be full ISO string
            try {
                SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                Date date = isoFormat.parse(dateStr.trim());
                if (date != null) {
                    SimpleDateFormat outFormat = new SimpleDateFormat("dd MMM, yyyy", Locale.getDefault());
                    return outFormat.format(date);
                }
            } catch (Exception ignored2) {}
        }
        return dateStr;
    }

    public static String todayDateString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }
}
