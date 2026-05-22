package com.example.proyectofinal;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
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

public class CalendarSheet extends BottomSheetDialogFragment {

    private MaterialCalendarView calendarView;
    private TextView tvDetalle;
    private final Set<CalendarDay> diasConRutinas = new HashSet<>();

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(d -> {
            BottomSheetDialog dlg = (BottomSheetDialog) d;
            View bottomSheet = dlg.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                bottomSheet.setBackgroundResource(R.drawable.bg_notif_sheet);
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
            }
            Window window = dlg.getWindow();
            if (window != null) {
                window.setNavigationBarColor(Color.TRANSPARENT);
            }
        });
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sheet_calendar, container, false);

        calendarView = view.findViewById(R.id.calendarView);
        tvDetalle = view.findViewById(R.id.tvRutinasDia);

        view.findViewById(R.id.btnCloseCalendar).setOnClickListener(v -> dismiss());

        // Estilo del calendario
        calendarView.setSelectionColor(Color.parseColor("#3384F527"));
        calendarView.setTileWidthDp(42);
        calendarView.setTileHeightDp(42);
        calendarView.setHeaderTextAppearance(R.style.CalendarHeader);
        calendarView.setWeekDayTextAppearance(R.style.CalendarWeekDay);
        calendarView.setDateTextAppearance(R.style.CalendarDate);

        calendarView.setOnDateChangedListener((widget, date, selected) -> {
            cargarRutinasDeFecha(formatCalendarDay(date));
        });

        calenarYMarcarRutinas();

        Calendar hoy = Calendar.getInstance();
        CalendarDay hoyDay = CalendarDay.from(hoy.get(Calendar.YEAR), hoy.get(Calendar.MONTH) + 1, hoy.get(Calendar.DAY_OF_MONTH));
        calendarView.setCurrentDate(hoyDay);
        calendarView.setSelectedDate(hoyDay);
        cargarRutinasDeFecha(formatCalendarDay(hoyDay));

        return view;
    }

    private void calenarYMarcarRutinas() {
        ApiService.getRutinasCalendario(requireContext(), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Error al cargar rutinas", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful() || response.body() == null) return;
                try {
                    JSONArray rutinasArray = new JSONArray(response.body().string());
                    Calendar ahora = Calendar.getInstance();
                    int mesActual = ahora.get(Calendar.MONTH);
                    int anoActual = ahora.get(Calendar.YEAR);

                    for (int i = 0; i < rutinasArray.length(); i++) {
                        JSONObject rutina = rutinasArray.getJSONObject(i);
                        String diasSemanaStr = rutina.optString("dias_semana", "");
                        if (diasSemanaStr.isEmpty()) continue;

                        String[] diasArray = diasSemanaStr.split(",");
                        Set<Integer> diasAsignados = new HashSet<>();
                        for (String diaStr : diasArray) {
                            try { diasAsignados.add(Integer.parseInt(diaStr.trim())); }
                            catch (NumberFormatException ignored) {}
                        }

                        Calendar cal = Calendar.getInstance();
                        cal.set(anoActual, mesActual, 1);
                        int diasEnMes = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

                        for (int dia = 1; dia <= diasEnMes; dia++) {
                            cal.set(anoActual, mesActual, dia);
                            int indiceDia = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7;
                            if (diasAsignados.contains(indiceDia)) {
                                diasConRutinas.add(CalendarDay.from(anoActual, mesActual + 1, dia));
                            }
                        }
                    }

                    requireActivity().runOnUiThread(() -> {
                        if (!diasConRutinas.isEmpty()) {
                            calendarView.addDecorator(new CircleDecorator(diasConRutinas, requireContext(), R.drawable.circle_green));
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void cargarRutinasDeFecha(String fecha) {
        ApiService.getRutinasPorFecha(requireContext(), fecha, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Error al cargar rutinas", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful() || response.body() == null) return;
                try {
                    JSONArray arr = new JSONArray(response.body().string());
                    StringBuilder sb = new StringBuilder();
                    if (arr.length() == 0) sb.append("Sin rutinas asignadas para este día.");
                    else {
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject r = arr.getJSONObject(i);
                            sb.append("• ").append(r.optString("nombre", "Rutina"));
                            int reps = r.optInt("repeticiones", 0);
                            if (reps > 0) sb.append("  —  ").append(reps).append(" reps");
                            sb.append("\n");
                        }
                    }
                    final String text = sb.toString().trim();
                    requireActivity().runOnUiThread(() -> tvDetalle.setText(text));
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private String formatCalendarDay(CalendarDay day) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar c = Calendar.getInstance();
        c.set(day.getYear(), day.getMonth() - 1, day.getDay());
        return sdf.format(c.getTime());
    }
}
