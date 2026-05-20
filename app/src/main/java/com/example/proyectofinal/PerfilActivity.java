package com.example.proyectofinal;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import java.io.IOException;

public class PerfilActivity extends AppCompatActivity {

    private TextView saludoText, usernameText, miembroDesdeText, nivelText;
    private TextView entrenamientosText, diasConsecutivosText, caloriasText, diasActivosText, rachaText, pesoText, alturaText;
    private ImageView profileImage;
    private Button btnLogout;
    private TextView rutinasFinalizadasText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        saludoText = findViewById(R.id.saludoText);
        usernameText = findViewById(R.id.usernameText);
        miembroDesdeText = findViewById(R.id.miembroDesdeText);
        nivelText = findViewById(R.id.nivelText);

        pesoText = findViewById(R.id.pesoText);
        alturaText = findViewById(R.id.alturaText);

        caloriasText = findViewById(R.id.caloriasText);
        diasActivosText = findViewById(R.id.diasActivosText);
        rachaText = findViewById(R.id.rachaText);
        rutinasFinalizadasText = findViewById(R.id.rutinasFinalizadasText);

        profileImage = findViewById(R.id.profile_image);
        btnLogout = findViewById(R.id.btnLogout);

        btnLogout.setOnClickListener(v -> logout());

        ImageView btnEditar = findViewById(R.id.btnEditProfile);
        btnEditar.setOnClickListener(v -> startActivity(new Intent(this, EditProfileActivity.class)));

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_perfil);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_inicio) {
                startActivity(new Intent(this, DashboardActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_rutinas) {
                startActivity(new Intent(this, RutinasActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_progreso) {
                startActivity(new Intent(this, ProgresoActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_perfil) {
                return true;
            }
            return false;
        });

        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String userId = prefs.getString("user_id", null);
        String token = prefs.getString("token", null);

        if (userId != null) {

            ApiService.getPerfil(this, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() ->
                            Toast.makeText(PerfilActivity.this, "Error al cargar perfil: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.body() == null) {
                        runOnUiThread(() ->
                                Toast.makeText(PerfilActivity.this, "Respuesta vacía del servidor", Toast.LENGTH_SHORT).show()
                        );
                        return;
                    }

                    String body = response.body().string();
                    runOnUiThread(() -> {
                        try {
                            if (!response.isSuccessful()) {
                                Toast.makeText(PerfilActivity.this, "Error al obtener datos: " + response.code(), Toast.LENGTH_LONG).show();
                                mostrarUsuarioVacio();
                                return;
                            }

                            JSONObject usuario = new JSONObject(body);
                            mostrarDatosUsuario(usuario);

                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(PerfilActivity.this, "Error al parsear perfil", Toast.LENGTH_LONG).show();
                            mostrarUsuarioVacio();
                        }
                    });
                }
            });

            if (token != null) cargarCalorias(token);
            actualizarDiasActivos();
            cargarRutinasFinalizadas();

        } else {
            mostrarUsuarioVacio();
        }
    }

    private void logout() {
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();

        Intent intent = new Intent(PerfilActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void mostrarUsuarioVacio() {
        saludoText.setText("¡Hola!");
        usernameText.setText("-");
        miembroDesdeText.setText("-");
        nivelText.setText("-");
        entrenamientosText.setText("0");
        diasConsecutivosText.setText("0");
        caloriasText.setText("0");
        diasActivosText.setText("0");
        rachaText.setText("Racha: 0 días 🔥");
    }

    private void mostrarDatosUsuario1(@NonNull JSONObject usuario) {

        String nombre = usuario.optString("nombre", "Usuario");
        String email = usuario.optString("email", "-");
        String fecha = usuario.optString("fecha_creacion", "-");

        saludoText.setText("¡Hola, " + nombre + "!");
        usernameText.setText(email);
        miembroDesdeText.setText("Miembro desde " + fecha.substring(0, 10));
        nivelText.setText(usuario.optString("nivel", "-"));

        entrenamientosText.setText(String.valueOf(usuario.optInt("entrenamientos", 0)));
        diasConsecutivosText.setText(String.valueOf(usuario.optInt("dias_consecutivos", 0)));
    }

    private void mostrarDatosUsuario2(@NonNull JSONObject usuario) {


        String nombre = usuario.optString("nombre", "Usuario");
        String email = usuario.optString("email", "-");

        String fecha = usuario.optString("fecha_creacion", "-");
        miembroDesdeText.setText("Miembro desde " + fecha.substring(0, 10));

        saludoText.setText("¡Hola, " + nombre + "!");
        usernameText.setText(email);
        nivelText.setText(usuario.optString("nivel", "-"));


        int entrenamientos = usuario.optInt("entrenamientos", -1);
        int diasConsec = usuario.optInt("dias_consecutivos", -1);


        if (entrenamientos == -1) {
            SharedPreferences prefs = getSharedPreferences("ActivityPrefs", MODE_PRIVATE);
            entrenamientos = prefs.getInt("dias_activos", 0);
        }
        if (diasConsec == -1) {
            SharedPreferences prefs = getSharedPreferences("ActivityPrefs", MODE_PRIVATE);
            diasConsec = prefs.getInt("racha", 0);
        }

        entrenamientosText.setText(String.valueOf(entrenamientos));
        diasConsecutivosText.setText(String.valueOf(diasConsec));


        caloriasText.setText(String.valueOf(usuario.optInt("calorias_totales", 0)));

    }

    private void mostrarDatosUsuario(@NonNull JSONObject usuario) {

        String nombre = usuario.optString("nombre", "Usuario");
        String email = usuario.optString("email", "-");

        String fecha = usuario.optString("fecha_creacion", "-");
        miembroDesdeText.setText("Miembro desde " + fecha.substring(0, 10));

        saludoText.setText("¡Hola, " + nombre + "!");
        usernameText.setText(email);
        nivelText.setText(usuario.optString("nivel", "-"));


        pesoText.setText(usuario.optString("peso", "-"));
        alturaText.setText(usuario.optString("altura", "-"));

        caloriasText.setText(String.valueOf(usuario.optInt("calorias_totales", 0)));
    }
    private void cargarCalorias(String token) {
        String url = "https://iatic.es/ifc302/g1/fitapp/api.php/dashboard";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    int calorias = response.optInt("calorias_quemadas", 0);
                    caloriasText.setText(String.valueOf(calorias));
                },
                error -> caloriasText.setText("0")
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    private void actualizarDiasActivos() {
        SharedPreferences prefs = getSharedPreferences("ActivityPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        String lastActive = prefs.getString("last_active_day", "");
        int diasActivos = prefs.getInt("dias_activos", 0);
        int racha = prefs.getInt("racha", 0);

        String hoy = java.time.LocalDate.now().toString();

        if (!lastActive.equals(hoy)) {
            diasActivos++;
            if (lastActive.equals(java.time.LocalDate.now().minusDays(1).toString())) {
                racha++;
            } else {
                racha = 1;
            }

            editor.putString("last_active_day", hoy);
            editor.putInt("dias_activos", diasActivos);
            editor.putInt("racha", racha);
            editor.apply();
        }

        diasActivosText.setText(String.valueOf(diasActivos));
        rachaText.setText("Racha: " + racha + " días 🔥");
    }

    private void cargarRutinasFinalizadas() {

        ApiService.getRutinas(this, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        rutinasFinalizadasText.setText("Rutinas finalizadas: 0")
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (response.body() == null) return;

                try {
                    String body = response.body().string();
                    org.json.JSONArray array = new org.json.JSONArray(body);

                    int totalFinalizadas = 0;

                    for (int i = 0; i < array.length(); i++) {
                        org.json.JSONObject rutina = array.getJSONObject(i);
                        if (rutina.optInt("completada", 0) == 1) {
                            totalFinalizadas++;
                        }
                    }

                    int finalTotal = totalFinalizadas;

                    runOnUiThread(() ->
                            rutinasFinalizadasText.setText(
                                    "Rutinas finalizadas: " + finalTotal
                            )
                    );

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
