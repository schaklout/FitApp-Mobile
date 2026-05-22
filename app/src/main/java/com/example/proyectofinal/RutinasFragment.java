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
            tvTitulo.setOnClickListener(v -> new CalendarRutinasDialog(requireActivity()).mostrar());
        }

        Button btnCalendario = view.findViewById(R.id.btnCalendario);
        if (btnCalendario != null) {
            btnCalendario.setOnClickListener(v -> new CalendarRutinasDialog(requireActivity()).mostrar());
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

                                    LinearLayout card = crearCardRutina(nombre, descripcion, nivel, rutinaUsuarioId,
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

    private LinearLayout crearCardRutina(String nombre, String descripcion, String nivel, int rutinaUsuarioId,
                                         int repeticiones, int vecesPorSemana, int diaSemana) {
        LinearLayout card = new LinearLayout(requireContext());
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(40, 40, 40, 40);
        card.setBackgroundColor(Color.parseColor("#1E1E2E"));
        card.setElevation(8f);
        card.setClipToPadding(false);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 32);
        card.setLayoutParams(params);

        TextView tvNombre = new TextView(requireContext());
        tvNombre.setText(nombre);
        tvNombre.setTextColor(Color.WHITE);
        tvNombre.setTextSize(18);
        tvNombre.setTypeface(null, Typeface.BOLD);

        TextView tvDescripcion = new TextView(requireContext());
        tvDescripcion.setText(descripcion);
        tvDescripcion.setTextColor(Color.parseColor("#AAAAAA"));
        tvDescripcion.setTextSize(14);
        tvDescripcion.setPadding(0, 8, 0, 8);

        TextView tvNivel = new TextView(requireContext());
        tvNivel.setText("Nivel: " + nivel);
        tvNivel.setTextColor(Color.parseColor("#9ED700"));
        tvNivel.setTextSize(14);

        card.addView(tvNombre);
        card.addView(tvDescripcion);
        card.addView(tvNivel);

        LinearLayout metaLine = new LinearLayout(requireContext());
        metaLine.setOrientation(LinearLayout.HORIZONTAL);
        metaLine.setPadding(0, 8, 0, 8);

        if (repeticiones > 0) {
            TextView tvReps = new TextView(requireContext());
            tvReps.setText(repeticiones + " reps");
            tvReps.setTextColor(Color.parseColor("#CCCCCC"));
            tvReps.setPadding(0, 0, 16, 0);
            metaLine.addView(tvReps);
        }

        if (vecesPorSemana > 0) {
            TextView tvVeces = new TextView(requireContext());
            tvVeces.setText(vecesPorSemana + "x/sem");
            tvVeces.setTextColor(Color.parseColor("#CCCCCC"));
            tvVeces.setPadding(0, 0, 16, 0);
            metaLine.addView(tvVeces);
        }

        if (diaSemana >= 0 && diaSemana <= 6) {
            String[] diasMap = {"Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom"};
            TextView tvDia = new TextView(requireContext());
            tvDia.setText("📅 " + diasMap[diaSemana]);
            tvDia.setTextColor(Color.parseColor("#AAAAAA"));
            metaLine.addView(tvDia);
        }

        if (metaLine.getChildCount() > 0) card.addView(metaLine);

        Button btnEntrenar = new Button(requireContext());
        btnEntrenar.setText("Entrenar");
        btnEntrenar.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#9ED700")));
        btnEntrenar.setTextColor(Color.BLACK);
        btnEntrenar.setPadding(0, 20, 0, 20);
        btnEntrenar.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), EntrenamientoActivity.class);
            intent.putExtra("rutinaUsuarioId", rutinaUsuarioId);
            entrenamientoLauncher.launch(intent);
        });
        card.addView(btnEntrenar);

        return card;
    }
}
