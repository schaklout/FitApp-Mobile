package com.example.proyectofinal;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class RutinasFragment extends Fragment {

    private LinearLayout linearRutinasDelDia;
    private TextView tvNoRutinas;
    private ActivityResultLauncher<Intent> entrenamientoLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        entrenamientoLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK) {
                        cargarRutinas();
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_rutinas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        linearRutinasDelDia = view.findViewById(R.id.linearRutinasActivas);
        tvNoRutinas = view.findViewById(R.id.tvNoRutinasActivas);

        TextView tvTitulo = view.findViewById(R.id.tvTituloRutinas);
        if (tvTitulo != null) {
            String hoy = obtenerDiaHoy();
            tvTitulo.setText("Rutinas - " + hoy);
            tvTitulo.setOnClickListener(v -> new CalendarSheet().show(getChildFragmentManager(), "calendar"));
        }

        Button btnCalendario = view.findViewById(R.id.btnCalendario);
        if (btnCalendario != null) {
            btnCalendario.setOnClickListener(v -> new CalendarSheet().show(getChildFragmentManager(), "calendar"));
        }

        cargarRutinas();
    }

    private String obtenerDiaHoy() {
        String[] dias = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};
        int dow = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        int idx = (dow + 5) % 7;
        return dias[idx];
    }

    private void cargarRutinas() {
        int dow = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        int dia = (dow + 5) % 7;

        ApiService.getRutinasPorDia(requireActivity(), dia, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireActivity(), "Error al obtener rutinas", Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() == null) return;
                if (response.isSuccessful()) {
                    String json = response.body().string();
                    try {
                        JSONArray rutinasArray = new JSONArray(json);
                        requireActivity().runOnUiThread(() -> {
                            linearRutinasDelDia.removeAllViews();
                            boolean hayRutinas = false;
                            for (int i = 0; i < rutinasArray.length(); i++) {
                                try {
                                    JSONObject rutina = rutinasArray.getJSONObject(i);
                                    String nombre = rutina.getString("nombre");
                                    String descripcion = rutina.optString("descripcion", "Sin descripción");
                                    String nivel = rutina.optString("nivel", "Nivel no especificado");
                                    int rutinaUsuarioId = rutina.optInt("rutina_usuario_id", rutina.optInt("id", -1));
                                    int repeticiones = rutina.optInt("repeticiones", 0);
                                    int vecesPorSemana = rutina.optInt("veces_por_semana", 0);
                                    int diaSemana = rutina.optInt("dia_semana", -1);

                                    View card = crearCardRutina(nombre, descripcion, nivel, rutinaUsuarioId,
                                            repeticiones, vecesPorSemana, diaSemana);
                                    linearRutinasDelDia.addView(card);
                                    hayRutinas = true;
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            tvNoRutinas.setVisibility(hayRutinas ? View.GONE : View.VISIBLE);
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireActivity(), "Error del servidor (" + response.code() + ")", Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }

    private View crearCardRutina(String nombre, String descripcion, String nivel, int rutinaUsuarioId,
                                         int repeticiones, int vecesPorSemana, int diaSemana) {
        com.google.android.material.card.MaterialCardView card = new com.google.android.material.card.MaterialCardView(requireContext());
        card.setCardBackgroundColor(Color.parseColor("#0D0D0D"));
        card.setStrokeColor(Color.parseColor("#3384F527"));
        card.setStrokeWidth(2);
        card.setRadius(24);
        card.setCardElevation(4f);
        card.setClipToPadding(false);
        card.setContentPadding(24, 24, 24, 24);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 20);
        card.setLayoutParams(params);

        LinearLayout content = new LinearLayout(requireContext());
        content.setOrientation(LinearLayout.VERTICAL);

        // Nivel chip
        LinearLayout topRow = new LinearLayout(requireContext());
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(android.view.Gravity.CENTER_VERTICAL);

        TextView tvNivel = new TextView(requireContext());
        tvNivel.setText(nivel.toUpperCase());
        tvNivel.setTextColor(Color.parseColor("#84F527"));
        tvNivel.setTextSize(10);
        tvNivel.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.barlow_bold));
        tvNivel.setBackgroundResource(R.drawable.bg_level);
        tvNivel.setPadding(12, 4, 12, 4);
        topRow.addView(tvNivel);

        if (diaSemana >= 0 && diaSemana <= 6) {
            String[] diasMap = {"Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom"};
            TextView tvDia = new TextView(requireContext());
            tvDia.setText(diasMap[diaSemana]);
            tvDia.setTextColor(Color.parseColor("#888888"));
            tvDia.setTextSize(11);
            tvDia.setPadding(16, 0, 0, 0);
            topRow.addView(tvDia);
        }

        content.addView(topRow);

        // Titulo
        TextView tvNombre = new TextView(requireContext());
        tvNombre.setText(nombre);
        tvNombre.setTextColor(Color.WHITE);
        tvNombre.setTextSize(22);
        tvNombre.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.bebas_neue));
        tvNombre.setPadding(0, 12, 0, 0);
        content.addView(tvNombre);

        // Descripcion
        TextView tvDescripcion = new TextView(requireContext());
        tvDescripcion.setText(descripcion);
        tvDescripcion.setTextColor(Color.parseColor("#999999"));
        tvDescripcion.setTextSize(13);
        tvDescripcion.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.barlow));
        tvDescripcion.setPadding(0, 6, 0, 0);
        tvDescripcion.setLineSpacing(4, 1);
        content.addView(tvDescripcion);

        // Metricas
        if (repeticiones > 0 || vecesPorSemana > 0) {
            LinearLayout metaLine = new LinearLayout(requireContext());
            metaLine.setOrientation(LinearLayout.HORIZONTAL);
            metaLine.setPadding(0, 14, 0, 0);

            if (repeticiones > 0) {
                TextView tvReps = new TextView(requireContext());
                tvReps.setText(repeticiones + " reps");
                tvReps.setTextColor(Color.parseColor("#AAAAAA"));
                tvReps.setTextSize(11);
                tvReps.setPadding(0, 0, 20, 0);
                metaLine.addView(tvReps);
            }

            if (vecesPorSemana > 0) {
                TextView tvVeces = new TextView(requireContext());
                tvVeces.setText(vecesPorSemana + "x/semana");
                tvVeces.setTextColor(Color.parseColor("#AAAAAA"));
                tvVeces.setTextSize(11);
                metaLine.addView(tvVeces);
            }

            content.addView(metaLine);
        }

        // Boton
        com.google.android.material.button.MaterialButton btnEntrenar = new com.google.android.material.button.MaterialButton(requireContext());
        btnEntrenar.setText("Empezar rutina");
        btnEntrenar.setTextColor(Color.BLACK);
        btnEntrenar.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#84F527")));
        btnEntrenar.setCornerRadius(100);
        btnEntrenar.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.barlow_bold));
        btnEntrenar.setTextSize(14);
        btnEntrenar.setLetterSpacing(0.05f);

        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        btnParams.topMargin = 20;
        btnEntrenar.setLayoutParams(btnParams);

        btnEntrenar.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), EntrenamientoActivity.class);
            intent.putExtra("rutinaUsuarioId", rutinaUsuarioId);
            entrenamientoLauncher.launch(intent);
        });

        content.addView(btnEntrenar);
        card.addView(content);

        return card;
    }
}
