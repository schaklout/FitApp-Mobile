package com.example.proyectofinal;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

public final class InsetsHelper {

    private InsetsHelper() {}

    public static void enable(Activity activity) {
        WindowCompat.setDecorFitsSystemWindows(activity.getWindow(), false);
    }

    public static void padTop(View view) {
        final int origLeft = view.getPaddingLeft();
        final int origTop = view.getPaddingTop();
        final int origRight = view.getPaddingRight();
        final int origBottom = view.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(origLeft, origTop + bars.top, origRight, origBottom);
            return insets;
        });
    }

    public static void padBottom(View view) {
        final int origLeft = view.getPaddingLeft();
        final int origTop = view.getPaddingTop();
        final int origRight = view.getPaddingRight();
        final int origBottom = view.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(origLeft, origTop, origRight, origBottom + bars.bottom);
            return insets;
        });
    }

    public static void padBoth(View view) {
        final int origLeft = view.getPaddingLeft();
        final int origTop = view.getPaddingTop();
        final int origRight = view.getPaddingRight();
        final int origBottom = view.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(origLeft, origTop + bars.top, origRight, origBottom + bars.bottom);
            return insets;
        });
    }

    public static void liftBottomNav(View view) {
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        final int origBottom = params.bottomMargin;
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            int bottom = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            lp.bottomMargin = origBottom + bottom;
            v.setLayoutParams(lp);
            return insets;
        });
    }
}
