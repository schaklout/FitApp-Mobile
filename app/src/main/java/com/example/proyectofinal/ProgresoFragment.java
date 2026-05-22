package com.example.proyectofinal;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ProgresoFragment extends Fragment {

    private LineChart lineChartCal, lineChartPeso;
    private TextView tvStatCalorias, tvStatSesiones, tvBadgeTrend, tvResumen;
    private MaterialCardView cardPeso;
    private LinearLayout chipContainer;

    private JSONArray allData = new JSONArray();
    private int currentDays = -1;
    private ArrayList<String> diasLabels = new ArrayList<>();
    private TextView activeChip;

    private final SimpleDateFormat API_DATE = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat LABEL_DATE = new SimpleDateFormat("dd/MM", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_progreso, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        lineChartCal = view.findViewById(R.id.lineChartCalorias);
        lineChartPeso = view.findViewById(R.id.lineChartPeso);
        tvStatCalorias = view.findViewById(R.id.tvStatCalorias);
        tvStatSesiones = view.findViewById(R.id.tvStatSesiones);
        tvBadgeTrend = view.findViewById(R.id.tvBadgeTrend);
        tvResumen = view.findViewById(R.id.tvResumenProgreso);
        cardPeso = view.findViewById(R.id.cardPeso);
        chipContainer = view.findViewById(R.id.chipContainer);

        configurarChart(lineChartCal);
        configurarChart(lineChartPeso);
        configurarChips();
        cargarProgreso();
    }

    private void configurarChart(LineChart chart) {
        chart.setDrawGridBackground(false);
        chart.setBackgroundColor(Color.TRANSPARENT);
        chart.setNoDataText("Cargando...");
        chart.setNoDataTextColor(Color.parseColor("#888888"));
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setPinchZoom(false);
        chart.setDoubleTapToZoomEnabled(false);
        chart.setDrawBorders(false);
        chart.getDescription().setEnabled(false);
        chart.animateX(800);
        chart.setExtraOffsets(4, 8, 4, 4);

        Legend legend = chart.getLegend();
        legend.setTextColor(Color.parseColor("#888888"));
        legend.setTextSize(11f);
        legend.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.barlow));
        legend.setForm(Legend.LegendForm.LINE);

        XAxis xAxis = chart.getXAxis();
        xAxis.setTextColor(Color.parseColor("#666666"));
        xAxis.setTextSize(10f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.barlow));
        xAxis.setGranularity(1f);

        YAxis yLeft = chart.getAxisLeft();
        yLeft.setTextColor(Color.parseColor("#666666"));
        yLeft.setTextSize(10f);
        yLeft.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.barlow));
        yLeft.setDrawGridLines(true);
        yLeft.setGridColor(Color.parseColor("#1A1A1A"));
        yLeft.setDrawAxisLine(false);
        yLeft.setGranularity(1f);

        chart.getAxisRight().setEnabled(false);
    }

    private void configurarChips() {
        String[] labels = {"7D", "30D", "Todo"};
        int[] values = {7, 30, -1};

        for (int i = 0; i < labels.length; i++) {
            TextView chip = new TextView(requireContext());
            chip.setText(labels[i]);
            chip.setTextColor(Color.parseColor("#888888"));
            chip.setTextSize(12f);
            chip.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.barlow_bold));
            chip.setPadding(28, 10, 28, 10);
            chip.setBackground(createChipBg(false));
            chip.setClickable(true);
            chip.setFocusable(true);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 12, 0);
            chip.setLayoutParams(params);

            final int days = values[i];
            chip.setOnClickListener(v -> {
                if (activeChip != null) activeChip.setBackground(createChipBg(false));
                activeChip = chip;
                activeChip.setBackground(createChipBg(true));
                currentDays = days;
                if (allData != null) actualizarVistaFiltrada(allData, days);
            });

            chipContainer.addView(chip);
            if (i == 2) {
                activeChip = chip;
                activeChip.setBackground(createChipBg(true));
            }
        }
    }

    private GradientDrawable createChipBg(boolean active) {
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setCornerRadius(100f);
        bg.setColor(active ? Color.parseColor("#2084F527") : Color.TRANSPARENT);
        bg.setStroke(1, Color.parseColor(active ? "#4084F527" : "#333333"));
        return bg;
    }

    private void cargarProgreso() {
        ApiService.getProgreso(requireActivity(), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                requireActivity().runOnUiThread(() -> lineChartCal.setNoDataText("Error cargando progreso."));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String body = response.body().string();
                    try {
                        JSONArray array = new JSONArray(body);
                        allData = array;
                        requireActivity().runOnUiThread(() -> actualizarVistaFiltrada(array, currentDays));
                    } catch (Exception e) {
                        requireActivity().runOnUiThread(() -> lineChartCal.setNoDataText("Error procesando los datos."));
                    }
                } else {
                    requireActivity().runOnUiThread(() -> lineChartCal.setNoDataText("No hay registros de progreso."));
                }
            }
        });
    }

    private Date parseFechaApi(String fechaStr) {
        if (fechaStr == null || fechaStr.isEmpty()) return null;
        try { return API_DATE.parse(fechaStr); } catch (ParseException e) {
            try { return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(fechaStr);
            } catch (Exception ex) { return null; }
        }
    }

    private double parsePesoSafe(JSONObject p) {
        try {
            if (p.has("peso")) {
                Object o = p.get("peso");
                if (o instanceof Number) return ((Number) o).doubleValue();
                String s = p.optString("peso", "").replace(",", ".");
                if (!s.isEmpty()) return Double.parseDouble(s);
            }
        } catch (Exception ignored) {}
        return Double.NaN;
    }

    private void actualizarVistaFiltrada(JSONArray progresoArray, int days) {
        try {
            if (progresoArray.length() == 0) {
                lineChartCal.clear();
                lineChartCal.setNoDataText("No hay registros de progreso aún.");
                lineChartCal.invalidate();
                lineChartPeso.clear();
                cardPeso.setVisibility(View.GONE);
                tvStatCalorias.setText("0");
                tvStatSesiones.setText("0");
                tvBadgeTrend.setText("0%");
                tvResumen.setText("");
                return;
            }

            ArrayList<JSONObject> lista = new ArrayList<>();
            for (int i = 0; i < progresoArray.length(); i++) lista.add(progresoArray.getJSONObject(i));

            Collections.sort(lista, (o1, o2) -> {
                Date d1 = parseFechaApi(o1.optString("fecha", ""));
                Date d2 = parseFechaApi(o2.optString("fecha", ""));
                if (d1 == null && d2 == null) return 0;
                if (d1 == null) return -1;
                if (d2 == null) return 1;
                return Long.compare(d1.getTime(), d2.getTime());
            });

            long umbral = Long.MIN_VALUE;
            if (days > 0) {
                Calendar c = Calendar.getInstance();
                c.add(Calendar.DAY_OF_YEAR, -(days - 1));
                umbral = c.getTimeInMillis();
            }

            ArrayList<Entry> calEntries = new ArrayList<>();
            ArrayList<Entry> pesoEntries = new ArrayList<>();
            ArrayList<String> labels = new ArrayList<>();
            int totalCalorias = 0, sesiones = 0;
            int calPrevias = 0;

            for (int i = 0; i < lista.size(); i++) {
                JSONObject p = lista.get(i);
                String fechaStr = p.optString("fecha", "");
                Date fecha = parseFechaApi(fechaStr);
                if (fecha == null) continue;
                if (days > 0 && fecha.getTime() < umbral) continue;

                int calorias = p.optInt("calorias_quemadas", p.optInt("calorias", 0));
                double peso = parsePesoSafe(p);

                calEntries.add(new Entry(sesiones, calorias));
                if (!Double.isNaN(peso)) pesoEntries.add(new Entry(sesiones, (float) peso));
                labels.add(LABEL_DATE.format(fecha));

                totalCalorias += calorias;
                if (sesiones == 0) calPrevias = calorias;
                sesiones++;
            }

            // Stats
            tvStatCalorias.setText(String.valueOf(totalCalorias));
            tvStatSesiones.setText(String.valueOf(sesiones));
            tvResumen.setText("Último registro: " + (labels.isEmpty() ? "-" : labels.get(labels.size() - 1)));

            // Trend badge
            if (sesiones >= 2 && calPrevias > 0) {
                int ultimas = (int) calEntries.get(calEntries.size() - 1).getY();
                float pct = ((float) (ultimas - calPrevias) / calPrevias) * 100f;
                String sign = pct >= 0 ? "+" : "";
                tvBadgeTrend.setText(sign + String.format("%.0f", pct) + "%");
                tvBadgeTrend.setTextColor(Color.parseColor(pct >= 0 ? "#84F527" : "#FF3B30"));
            }

            // Calories chart
            LineDataSet setCal = new LineDataSet(calEntries, "Calorías");
            setCal.setColor(Color.parseColor("#84F527"));
            setCal.setCircleColor(Color.parseColor("#84F527"));
            setCal.setLineWidth(2.5f);
            setCal.setCircleRadius(4f);
            setCal.setDrawValues(false);
            setCal.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            setCal.setDrawFilled(true);
            setCal.setFillColor(Color.parseColor("#1084F527"));
            setCal.setHighLightColor(Color.parseColor("#84F527"));
            setCal.enableDashedHighlightLine(10f, 4f, 0f);

            // Dotted style
            setCal.setDrawCircleHole(false);
            setCal.setDrawCircles(true);

            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(setCal);

            // Weight chart
            if (!pesoEntries.isEmpty()) {
                cardPeso.setVisibility(View.VISIBLE);
                LineDataSet setPeso = new LineDataSet(pesoEntries, "Peso (kg)");
                setPeso.setColor(Color.parseColor("#FFC107"));
                setPeso.setCircleColor(Color.parseColor("#FFC107"));
                setPeso.setLineWidth(2f);
                setPeso.setCircleRadius(3f);
                setPeso.setDrawValues(false);
                setPeso.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                setPeso.setDrawCircleHole(false);
                setPeso.setAxisDependency(YAxis.AxisDependency.LEFT);

                lineChartPeso.clear();
                LineData pesoData = new LineData(setPeso);
                lineChartPeso.setData(pesoData);
                formatXAxis(lineChartPeso.getXAxis(), labels);
                lineChartPeso.invalidate();
            } else {
                cardPeso.setVisibility(View.GONE);
            }

            lineChartCal.clear();
            LineData lineData = new LineData(dataSets);
            lineChartCal.setData(lineData);
            formatXAxis(lineChartCal.getXAxis(), labels);

            // Space for legend
            lineChartCal.setExtraOffsets(4, 8, 4, 24);
            lineChartCal.invalidate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void formatXAxis(XAxis xAxis, ArrayList<String> labels) {
        diasLabels = labels;
        xAxis.setLabelCount(Math.min(labels.size(), 6), true);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = Math.round(value);
                if (index >= 0 && index < labels.size()) return labels.get(index);
                return "";
            }
        });
    }
}
