package com.example.proyectofinal;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class EntrenamientoActivity extends AppCompatActivity {

    private TextView tvTituloEntrenamiento;
    private TextView tvDescripcionEntrenamiento;
    private TextView tvMetricasEntrenamiento;
    private ImageView ivImagenEntrenamiento;
    private VideoView videoRutina;
    private ProgressBar progressLoader;
    private LinearLayout linearEjercicios;
    private Button btnFinalizarEntrenamiento;
    private Button btnSalirEntrenamiento;
    private Chronometer chronometer;

    private int rutinaUsuarioId;
    private long tiempoInicio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        InsetsHelper.enable(this);
        setContentView(R.layout.activity_entrenamiento);
        InsetsHelper.padBoth(findViewById(R.id.entrenoScroll));

        tvTituloEntrenamiento = findViewById(R.id.tvTituloEntrenamiento);
        tvDescripcionEntrenamiento = findViewById(R.id.tvDescripcionEntrenamiento);
        tvMetricasEntrenamiento = findViewById(R.id.tvMetricasEntrenamiento);
        ivImagenEntrenamiento = findViewById(R.id.ivImagenEntrenamiento);
        videoRutina = findViewById(R.id.videoRutina);
        progressLoader = findViewById(R.id.progressLoader);
        linearEjercicios = findViewById(R.id.linearEjercicios);
        btnFinalizarEntrenamiento = findViewById(R.id.btnFinalizarEntrenamiento);
        btnSalirEntrenamiento = findViewById(R.id.btnSalirEntrenamiento);
        chronometer = findViewById(R.id.chronometer);

        // Recibe rutinaUsuarioId
        rutinaUsuarioId = getIntent().getIntExtra("rutinaUsuarioId",
                getIntent().getIntExtra("rutinaId", -1));

        if (rutinaUsuarioId != -1) {
            cargarRutina();
            iniciarCronometro();
        } else {
            Toast.makeText(this, "Error: ID de rutina no válido", Toast.LENGTH_SHORT).show();
            finish();
        }

        btnFinalizarEntrenamiento.setOnClickListener(v -> finalizarEntrenamiento());
        btnSalirEntrenamiento.setOnClickListener(v -> {
            detenerCronometro();
            finish();
        });
    }

    private void iniciarCronometro() {
        tiempoInicio = SystemClock.elapsedRealtime();
        if (chronometer != null) {
            chronometer.setBase(tiempoInicio);
            chronometer.start();
        }
    }

    private long detenerCronometro() {
        if (chronometer != null) {
            chronometer.stop();
            return (SystemClock.elapsedRealtime() - tiempoInicio) / 1000; // segundos
        }
        return 0;
    }

    private void cargarRutina() {
        ApiService.getRutinas(this, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(EntrenamientoActivity.this,
                                "Error al cargar rutina",
                                Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JSONArray array = new JSONArray(response.body().string());

                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);

                            int rid = obj.optInt("rutina_usuario_id",
                                    obj.optInt("id", -1));

                            if (rid == rutinaUsuarioId) {
                                runOnUiThread(() -> mostrarRutina(obj));
                                return;
                            }
                        }

                        runOnUiThread(() ->
                                Toast.makeText(EntrenamientoActivity.this,
                                        "Rutina no encontrada",
                                        Toast.LENGTH_SHORT).show()
                        );

                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() ->
                                Toast.makeText(EntrenamientoActivity.this,
                                        "Error al parsear respuesta",
                                        Toast.LENGTH_SHORT).show()
                        );
                    }
                }
            }
        });
    }

    private void mostrarRutina(JSONObject rutina) {
        try {
            tvTituloEntrenamiento.setText(rutina.optString("nombre", "Sin título"));
            tvDescripcionEntrenamiento.setText(rutina.optString("descripcion", "Sin descripción"));

            // Mostrar métricas
            StringBuilder metricas = new StringBuilder();

            int repeticiones = rutina.optInt("repeticiones", 0);
            if (repeticiones > 0) {
                metricas.append("Repeticiones: ").append(repeticiones).append(" | ");
            }

            int vecesPorSemana = rutina.optInt("veces_por_semana", 0);
            if (vecesPorSemana > 0) {
                metricas.append("Frecuencia: ").append(vecesPorSemana).append("x/sem");
            }

            if (metricas.length() > 0 && tvMetricasEntrenamiento != null) {
                tvMetricasEntrenamiento.setText(metricas.toString());
                tvMetricasEntrenamiento.setVisibility(View.VISIBLE);
            }

            // Multimedia
            String videoUrl = rutina.optString("video_url", "").trim();
            String imagenUrl = rutina.optString("imagen_url", "").trim();

            if (!videoUrl.isEmpty()) {
                if (!videoUrl.startsWith("http")) {
                    videoUrl = "https://iatic.es/ifc302/g1/fitapp/public/" + videoUrl;
                }

                progressLoader.setVisibility(View.VISIBLE);
                videoRutina.setVisibility(View.VISIBLE);
                ivImagenEntrenamiento.setVisibility(View.GONE);

                videoRutina.setVideoURI(Uri.parse(videoUrl));

                videoRutina.setOnPreparedListener(mp -> {
                    mp.setLooping(true);
                    videoRutina.start();
                    progressLoader.setVisibility(View.GONE);
                });

                videoRutina.setOnErrorListener((mp, what, extra) -> {
                    videoRutina.setVisibility(View.GONE);
                    cargarImagen(imagenUrl);
                    return true;
                });

            } else {
                videoRutina.setVisibility(View.GONE);
                cargarImagen(imagenUrl);
            }

            // Ejercicios
            linearEjercicios.removeAllViews();

            if (rutina.has("ejercicios")) {
                JSONArray ejercicios = rutina.getJSONArray("ejercicios");

                for (int i = 0; i < ejercicios.length(); i++) {
                    JSONObject e = ejercicios.getJSONObject(i);

                    String texto =
                            e.optString("nombre", "") + ": " +
                                    e.optInt("repeticiones", 0) + " reps, " +
                                    e.optInt("sets", 0) + " sets, descanso " +
                                    e.optInt("descanso", 0) + "s";

                    TextView tv = new TextView(this);
                    tv.setText(texto);
                    tv.setTextColor(Color.WHITE);
                    tv.setTextSize(16);
                    tv.setPadding(0, 8, 0, 8);

                    linearEjercicios.addView(tv);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            progressLoader.setVisibility(View.GONE);
            Toast.makeText(this, "Error al mostrar rutina", Toast.LENGTH_SHORT).show();
        }
    }

    private void cargarImagen(String imagenUrl) {
        if (!imagenUrl.isEmpty() && !imagenUrl.startsWith("http")) {
            imagenUrl = "https://iatic.es/ifc302/g1/fitapp/public/" + imagenUrl;
        }

        progressLoader.setVisibility(View.VISIBLE);
        ivImagenEntrenamiento.setVisibility(View.VISIBLE);

        Glide.with(this)
                .load(imagenUrl.isEmpty()
                        ? R.drawable.ic_rutina_placeholder
                        : imagenUrl)
                .into(ivImagenEntrenamiento);

        progressLoader.setVisibility(View.GONE);
    }

    private void finalizarEntrenamiento() {
        // Obtener tiempo de entrenamiento en minutos
        long segundos = detenerCronometro();

        final int minutos = Math.max(1, (int) (segundos / 60)); // final + Math.max evita reasignación

        // Mostrar diálogo para completar información (opcional)

        // Mostrar diálogo para completar información (opcional)
        int calorias = 0; // podría preguntar al usuario
        String notas = ""; // podría capturar notas

        ApiService.guardarSesionEntrenamiento(this, rutinaUsuarioId, minutos, calorias, notas,
                new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        runOnUiThread(() ->
                                Toast.makeText(EntrenamientoActivity.this,
                                        "Error al guardar sesión",
                                        Toast.LENGTH_SHORT).show()
                        );
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        runOnUiThread(() -> {
                            if (response.isSuccessful()) {
                                Toast.makeText(EntrenamientoActivity.this,
                                        "Entrenamiento completado: " + minutos + " minutos",
                                        Toast.LENGTH_SHORT).show();
                                setResult(RESULT_OK);
                                finish();
                            } else {
                                Toast.makeText(EntrenamientoActivity.this,
                                        "Error API: " + response.code(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
    }
}