package com.example.proyectofinal;

import android.app.AlertDialog;
import android.content.Context;
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

public class CalendarRutinasDialog {

    private final Context context;
    private final MaterialCalendarView calendarView;
    private final Set<CalendarDay> diasConRutinas = new HashSet<>();

    public CalendarRutinasDialog(Context ctx) {
        this.context = ctx;
        this.calendarView = new MaterialCalendarView(ctx);
    }

    public void mostrar() {
        AlertDialog.Builder b = new AlertDialog.Builder(context);
        b.setTitle("Calendario de Rutinas");

        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(16, 16, 16, 16);

        calendarView.setTileHeight(72);
        calendarView.setSelectionColor(Color.TRANSPARENT);
        root.addView(calendarView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView tvDetalle = new TextView(context);
        tvDetalle.setTextColor(Color.WHITE);
        tvDetalle.setText("Selecciona un día...");
        tvDetalle.setPadding(0, 12, 0, 0);
        root.addView(tvDetalle);

        b.setView(root);
        b.setNegativeButton("Cerrar", (d, i) -> d.dismiss());
        AlertDialog dialog = b.create();
        dialog.show();

        // Cargar todas las rutinas y calcular fechas del mes
        cargarYMarcarRutinasDelMes();

        // Listener: click en día -> mostrar rutinas de esa fecha
        calendarView.setOnDateChangedListener((widget, date, selected) -> {
            String fecha = formatCalendarDay(date);
            cargarRutinasDeFecha(fecha, tvDetalle);
        });

        // Mostrar hoy por defecto
        Calendar hoy = Calendar.getInstance();
        CalendarDay hoyDay = CalendarDay.from(hoy.get(Calendar.YEAR), hoy.get(Calendar.MONTH) + 1, hoy.get(Calendar.DAY_OF_MONTH));
        calendarView.setCurrentDate(hoyDay);
        cargarRutinasDeFecha(formatCalendarDay(hoyDay), tvDetalle);
    }

    /**
     * Carga TODAS las rutinas y calcula todos los días del mes donde hay asignadas
     */
    private void cargarYMarcarRutinasDelMes() {
        ApiService.getRutinasCalendario(context, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                ((android.app.Activity) context).runOnUiThread(() ->
                        Toast.makeText(context, "Error al cargar rutinas", Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful() || response.body() == null) return;

                try {
                    JSONArray rutinasArray = new JSONArray(response.body().string());

                    for (int i = 0; i < rutinasArray.length(); i++) {
                        JSONObject rutina = rutinasArray.getJSONObject(i);
                        String fecha = rutina.optString("fecha_asignacion", "");
                        if (fecha.isEmpty()) continue;

                        try {
                            String[] parts = fecha.split("-");
                            int year = Integer.parseInt(parts[0]);
                            int month = Integer.parseInt(parts[1]);
                            int day = Integer.parseInt(parts[2]);
                            diasConRutinas.add(CalendarDay.from(year, month, day));
                        } catch (Exception ignored) {}
                    }

                    ((android.app.Activity) context).runOnUiThread(() -> {
                        if (!diasConRutinas.isEmpty()) {
                            calendarView.addDecorator(new CircleDecorator(diasConRutinas, context, R.drawable.circle_yellow));
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Carga rutinas para una fecha específica y las muestra en el TextView
     */
    private void cargarRutinasDeFecha(String fecha, TextView tvDetalle) {
        ApiService.getRutinasPorFecha(context, fecha, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                ((android.app.Activity) context).runOnUiThread(() ->
                        Toast.makeText(context, "Error al cargar rutinas", Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful() || response.body() == null) return;
                try {
                    JSONArray arr = new JSONArray(response.body().string());
                    StringBuilder sb = new StringBuilder();
                    if (arr.length() == 0) sb.append("Sin rutinas asignadas.");
                    else {
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject r = arr.getJSONObject(i);
                            sb.append("• ").append(r.optString("nombre", ""));
                            sb.append("\n");
                        }
                    }
                    final String text = sb.toString();
                    ((android.app.Activity) context).runOnUiThread(() -> tvDetalle.setText(text));
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