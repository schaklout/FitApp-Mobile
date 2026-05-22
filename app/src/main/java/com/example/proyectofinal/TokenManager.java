package com.example.proyectofinal;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class TokenManager {

    private static final String PREFS_NAME = "MyAppPrefs";
    private static final String TOKEN_KEY = "token";
    private static final String PREF_NAME = "user_prefs";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_ONBOARDING_COMPLETED = "onboarding_completed";



    private static final String USER_NOMBRE = "user_nombre";
    private static final String USER_PESO = "user_peso";
    private static final String USER_ALTURA = "user_altura";
    private static final String USER_EMAIL = "user_email";
    // ==========================
    // 💾 GUARDAR TOKEN
    // ==========================
    public static void saveToken(Context context, String token) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(TOKEN_KEY, token);
        editor.apply();
    }

    public static void saveUsuario(Context context, String id,String nombre,String peso,String altura,String email) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(USER_NOMBRE, nombre);
        editor.putString(KEY_USER_ID, id);
        editor.putString(USER_PESO, peso);
        editor.putString(USER_ALTURA, altura);
        editor.putString(USER_EMAIL, email);
        editor.apply();
    }

    // ==========================
    // 🔍 OBTENER TOKEN
    // ==========================
    public static String getToken(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(TOKEN_KEY, "");
    }

    // ==========================
    // 🗑️ BORRAR TOKEN
    // ==========================
    public static void clearToken(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(TOKEN_KEY);
        editor.apply();
    }

    public static int getUserId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String idStr = prefs.getString(KEY_USER_ID, "-1");
        try {
            return Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public static String getUserNombre(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(USER_NOMBRE, "");
    }

    public static String getUserPeso(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(USER_PESO, "");
    }

    public static String getUserAltura(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(USER_ALTURA, "");
    }

    public static boolean isOnboardingCompleted(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false);
    }

    public static void setOnboardingCompleted(Context context, boolean completed) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_ONBOARDING_COMPLETED, completed);
        editor.apply();
    }
}

