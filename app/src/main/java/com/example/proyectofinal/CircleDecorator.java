package com.example.proyectofinal;

import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

import java.util.Collection;
import java.util.HashSet;

public class CircleDecorator implements DayViewDecorator {
    private final Drawable drawable;
    private final HashSet<CalendarDay> dates;

    public CircleDecorator(Collection<CalendarDay> dates, Context ctx, int drawableResId) {
        this.drawable = ContextCompat.getDrawable(ctx, drawableResId);
        this.dates = new HashSet<>(dates);
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return dates.contains(day);
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.setSelectionDrawable(drawable);
    }
}