package com.example.proyectofinal;

import java.util.Calendar;

public class EventDay {
    private Calendar calendar;
    private int iconRes;
    private int color;

    public EventDay(Calendar calendar, int iconRes, int color) {
        this.calendar = calendar;
        this.iconRes = iconRes;
        this.color = color;
    }

    public Calendar getCalendar() {
        return calendar;
    }

    public int getIconRes() {
        return iconRes;
    }

    public int getColor() {
        return color;
    }
}