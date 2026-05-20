package com.example.proyectofinal;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ProgresoActivity extends AppCompatActivity {

    private LineChart lineChart;
    private ArrayList<String> diasLabels = new ArrayList<>();
    private TextView tvResumen;



    private Spinner spinnerRango;
    private JSONArray allData = new JSONArray();

    private static final String TAG = "ProgresoActivity";
    private final SimpleDateFormat API_DATE = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat LABEL_DATE = new SimpleDateFormat("dd/MM", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progreso);

        lineChart = findViewById(R.id.lineChartCalorias);
        tvResumen = findViewById(R.id.tvResumenProgreso);
        spinnerRango = findViewById(R.id.spinnerRango);

        configurarChart();
        configurarSpinner();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_progreso);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_inicio) {
                startActivity(new Intent(this, DashboardActivity.class));
                return true;
            } else if (id == R.id.nav_rutinas) {
                startActivity(new Intent(this, RutinasActivity.class));
                return true;
            } else if (id == R.id.nav_perfil) {
                startActivity(new Intent(this, PerfilActivity.class));
                return true;
            } else if (id == R.id.nav_progreso) {
                return true;
            }
            return false;
        });

        cargarProgreso();
    }

    private void configurarSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"Últimos 7 días", "Últimos 30 días", "Todo"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRango.setAdapter(adapter);
        spinnerRango.setSelection(2); // "Todo" es el índice 2
        spinnerRango.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int days = position == 0 ? 7 : (position == 1 ? 30 : -1);
                if (allData != null) actualizarVistaFiltrada(allData, days);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void configurarChart() {
        lineChart.setNoDataText("Cargando progreso...");
        lineChart.setDrawGridBackground(false);
        lineChart.getLegend().setTextColor(0xFFFFFFFF);
        lineChart.animateX(800);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setTextColor(0xFFFFFFFF);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        YAxis yAxisLeft = lineChart.getAxisLeft();
        yAxisLeft.setTextColor(0xFFFFFFFF);

        YAxis right = lineChart.getAxisRight();
        right.setTextColor(0xFFFFFFFF);
        right.setEnabled(true);
    }

    private void cargarProgreso() {
        ApiService.getProgreso(this, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> lineChart.setNoDataText("Error cargando progreso."));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String body = response.body().string();
                    try {
                        JSONArray array = new JSONArray(body);
                        allData = array;
                        runOnUiThread(() -> actualizarVistaFiltrada(array, -1)); // Mostrar TODO por defecto
                    } catch (Exception e) {
                        runOnUiThread(() -> lineChart.setNoDataText("Error procesando los datos."));
                    }
                } else {
                    runOnUiThread(() -> lineChart.setNoDataText("No hay registros de progreso."));
                }
            }
        });
    }
    private Date parseFechaApi(String fechaStr) {
        if (fechaStr == null || fechaStr.isEmpty()) return null;
        try {
            return API_DATE.parse(fechaStr);
        } catch (ParseException e) {
            // intenta otras variantes si las hubiese
            try {
                return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(fechaStr);
            } catch (Exception ex) {
                return null;
            }
        }
    }

    private double parsePesoSafe(JSONObject p) {
        try {
            if (p.has("peso")) {
                Object o = p.get("peso");
                if (o instanceof Number) return ((Number)o).doubleValue();
                String s = p.optString("peso", "").replace(",", ".");
                if (!s.isEmpty()) return Double.parseDouble(s);
            }
        } catch (Exception ignored) {}
        return Double.NaN;
    }

    private void actualizarVistaFiltrada(JSONArray progresoArray, int days) {
        try {
            if (progresoArray.length() == 0) {
                lineChart.clear();
                lineChart.setNoDataText("No hay registros de progreso aún.");
                lineChart.invalidate();

                tvResumen.setText("");
                return;
            }

            ArrayList<Entry> caloriasEntries = new ArrayList<>();
            ArrayList<Entry> pesoEntries = new ArrayList<>();
            diasLabels.clear();

            // preparar lista de objetos con fecha parsed
            ArrayList<JSONObject> lista = new ArrayList<>();
            for (int i = 0; i < progresoArray.length(); i++) lista.add(progresoArray.getJSONObject(i));

            // ordenar por fecha ascendente para gráfico (API devuelve desc)
            Collections.sort(lista, new Comparator<JSONObject>() {
                @Override
                public int compare(JSONObject o1, JSONObject o2) {
                    Date d1 = parseFechaApi(o1.optString("fecha", ""));
                    Date d2 = parseFechaApi(o2.optString("fecha", ""));
                    if (d1 == null && d2 == null) return 0;
                    if (d1 == null) return -1;
                    if (d2 == null) return 1;
                    return Long.compare(d1.getTime(), d2.getTime());
                }
            });

            // calcular umbral si aplica
            long umbral = Long.MIN_VALUE;
            if (days > 0) {
                Calendar c = Calendar.getInstance();
                c.add(Calendar.DAY_OF_YEAR, - (days - 1));
                umbral = c.getTimeInMillis();
            }

            int idx = 0;
            int totalCalorias = 0;
            int sesiones = 0;
            double pesoUltimo = Double.NaN;
            String fechaUltimo = "";

            for (JSONObject p : lista) {
                String fechaStr = p.optString("fecha", "");
                Date fecha = parseFechaApi(fechaStr);
                if (fecha == null) continue;
                if (days > 0 && fecha.getTime() < umbral) continue;

                int calorias = p.optInt("calorias_quemadas", p.optInt("calorias", 0));
                double peso = parsePesoSafe(p);

                String label = LABEL_DATE.format(fecha);
                diasLabels.add(label);

                caloriasEntries.add(new Entry(idx, calorias));
                if (!Double.isNaN(peso)) pesoEntries.add(new Entry(idx, (float)peso));

                totalCalorias += calorias;
                sesiones++;

                // track último (más reciente)
                if (fechaUltimo.isEmpty() || parseFechaApi(fechaUltimo).before(fecha)) {
                    fechaUltimo = fechaStr;
                    pesoUltimo = peso;
                }
                idx++;
            }
            tvResumen.setText("Total calorías: " + totalCalorias + " • Registros: " + sesiones +
                    (fechaUltimo.isEmpty() ? "" : " • Últ: " + fechaUltimo));

            LineDataSet dataSetCal = new LineDataSet(caloriasEntries, "Calorías");
            dataSetCal.setColor(0xFF9ED700);
            dataSetCal.setCircleColor(0xFF9ED700);
            dataSetCal.setLineWidth(2f);
            dataSetCal.setCircleRadius(3f);
            dataSetCal.setValueTextColor(0xFFFFFFFF);
            dataSetCal.setMode(LineDataSet.Mode.CUBIC_BEZIER);

            LineData lineData = new LineData();
            lineData.addDataSet(dataSetCal);

            if (!pesoEntries.isEmpty()) {
                LineDataSet dataSetPeso = new LineDataSet(pesoEntries, "Peso (kg)");
                dataSetPeso.setColor(0xFFFFC107);
                dataSetPeso.setCircleColor(0xFFFFC107);
                dataSetPeso.setLineWidth(2f);
                dataSetPeso.setCircleRadius(3f);
                dataSetPeso.setValueTextColor(0xFFFFFFFF);
                dataSetPeso.setAxisDependency(YAxis.AxisDependency.RIGHT);
                lineData.addDataSet(dataSetPeso);
                lineChart.getAxisRight().setEnabled(true);
            } else {
                lineChart.getAxisRight().setEnabled(false);
            }

            lineChart.setData(lineData);

            XAxis xAxis = lineChart.getXAxis();
            xAxis.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    int index = Math.round(value);
                    if (index >= 0 && index < diasLabels.size()) return diasLabels.get(index);
                    return "";
                }
            });

            lineChart.invalidate();

        } catch (Exception e) {
            // Error al actualizar vista
        }
    }
}