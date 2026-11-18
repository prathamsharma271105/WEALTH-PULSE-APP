package com.example.financialtracker.utils;

import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.TypedValue;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.core.widget.ImageViewCompat;
import com.example.financialtracker.R;

public class PasswordUtils {

    /**
     * Attaches a show/hide password toggle action to an ImageView for a given password EditText.
     * Preserves cursor position, selection, and custom font typeface.
     */
    public static void setupPasswordToggle(EditText editText, ImageView toggleView) {
        if (editText == null || toggleView == null) return;

        final boolean[] isVisible = {false};

        // Resolve themed active and dim colors
        TypedValue valActive = new TypedValue();
        TypedValue valDim = new TypedValue();
        editText.getContext().getTheme().resolveAttribute(R.attr.colorAccentBrand, valActive, true);
        editText.getContext().getTheme().resolveAttribute(R.attr.colorTextDim, valDim, true);

        int colorActive = valActive.data;
        int colorDim = valDim.data;

        // Default state: hidden (dots)
        toggleView.setImageResource(R.drawable.ic_visibility_off);
        ImageViewCompat.setImageTintList(toggleView, ColorStateList.valueOf(colorDim));

        toggleView.setOnClickListener(v -> {
            isVisible[0] = !isVisible[0];
            Typeface currentTypeface = editText.getTypeface();
            int selectionStart = editText.getSelectionStart();
            int selectionEnd = editText.getSelectionEnd();

            if (isVisible[0]) {
                // Show Password
                editText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                toggleView.setImageResource(R.drawable.ic_visibility);
                ImageViewCompat.setImageTintList(toggleView, ColorStateList.valueOf(colorActive));
            } else {
                // Hide Password
                editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                toggleView.setImageResource(R.drawable.ic_visibility_off);
                ImageViewCompat.setImageTintList(toggleView, ColorStateList.valueOf(colorDim));
            }

            // Restore typeface & selection range
            editText.setTypeface(currentTypeface);
            if (selectionStart >= 0 && selectionEnd >= 0) {
                int length = editText.getText().length();
                editText.setSelection(Math.min(selectionStart, length), Math.min(selectionEnd, length));
            }
        });
    }
}

