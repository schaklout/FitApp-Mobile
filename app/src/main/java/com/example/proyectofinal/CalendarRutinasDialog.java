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

                    // Obtener mes/año actual
                    Calendar ahora = Calendar.getInstance();
                    int mesActual = ahora.get(Calendar.MONTH); // 0-based
                    int anoActual = ahora.get(Calendar.YEAR);

                    // Por cada rutina, calcular todos los días del mes donde se ejecuta
                    for (int i = 0; i < rutinasArray.length(); i++) {
                        JSONObject rutina = rutinasArray.getJSONObject(i);
                        String diasSemanaStr = rutina.optString("dias_semana", "");

                        if (!diasSemanaStr.isEmpty()) {
                            // dias_semana es "0,2,5" (0=Lun, 1=Mar, etc)
                            String[] diasArray = diasSemanaStr.split(",");
                            Set<Integer> diasAsignados = new HashSet<>();

                            for (String diaStr : diasArray) {
                                try {
                                    int dia = Integer.parseInt(diaStr.trim());
                                    diasAsignados.add(dia); // 0=Lun, 1=Mar, ..., 6=Dom
                                } catch (NumberFormatException e) {
                                    // ignorar
                                }
                            }

                            // Recorrer todos los días del mes y marcar los que coinciden
                            Calendar cal = Calendar.getInstance();
                            cal.set(anoActual, mesActual, 1);
                            int diasEnMes = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

                            for (int dia = 1; dia <= diasEnMes; dia++) {
                                cal.set(anoActual, mesActual, dia);

                                // Convertir Calendar.DAY_OF_WEEK a índice 0-6 (0=Lun)
                                int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
                                int indiceDia = (dayOfWeek + 5) % 7; // Convertir a 0=Lun...6=Dom

                                // Si este día de la semana está asignado, agregarlo
                                if (diasAsignados.contains(indiceDia)) {
                                    CalendarDay cd = CalendarDay.from(anoActual, mesActual + 1, dia);
                                    diasConRutinas.add(cd);
                                }
                            }
                        }
                    }

                    // Aplicar decorador en el hilo UI
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
                            int reps = r.optInt("repeticiones", 0);
                            if (reps > 0) sb.append(" (").append(reps).append(" reps)");
                            int veces = r.optInt("veces_por_semana", 0);
                            if (veces > 0) sb.append(" [").append(veces).append("x/sem]");
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