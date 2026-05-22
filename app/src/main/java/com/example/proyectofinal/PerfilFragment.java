package com.example.proyectofinal;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import java.io.IOException;

public class PerfilFragment extends Fragment {

    private TextView saludoText, usernameText, miembroDesdeText, nivelText;
    private TextView caloriasText, diasActivosText, rachaText, pesoText, alturaText;
    private ImageView profileImage;
    private Button btnLogout;
    private TextView rutinasFinalizadasText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_perfil, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        saludoText = view.findViewById(R.id.saludoText);
        usernameText = view.findViewById(R.id.usernameText);
        miembroDesdeText = view.findViewById(R.id.miembroDesdeText);
        nivelText = view.findViewById(R.id.nivelText);

        pesoText = view.findViewById(R.id.pesoText);
        alturaText = view.findViewById(R.id.alturaText);

        caloriasText = view.findViewById(R.id.caloriasText);
        diasActivosText = view.findViewById(R.id.diasActivosText);
        rachaText = view.findViewById(R.id.rachaText);
        rutinasFinalizadasText = view.findViewById(R.id.rutinasFinalizadasText);

        profileImage = view.findViewById(R.id.profile_image);
        btnLogout = view.findViewById(R.id.btnLogout);

        btnLogout.setOnClickListener(v -> logout());

        ImageView btnEditar = view.findViewById(R.id.btnEditProfile);
        btnEditar.setOnClickListener(v -> startActivity(new Intent(requireActivity(), EditProfileActivity.class)));

        SharedPreferences prefs = requireActivity().getSharedPreferences("MyAppPrefs", requireActivity().MODE_PRIVATE);
        String userId = prefs.getString("user_id", null);
        String token = prefs.getString("token", null);

        if (userId != null) {
            ApiService.getPerfil(requireActivity(), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireActivity(), "Error al cargar perfil: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.body() == null) {
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(requireActivity(), "Respuesta vacía del servidor", Toast.LENGTH_SHORT).show()
                        );
                        return;
                    }

                    String body = response.body().string();
                    requireActivity().runOnUiThread(() -> {
                        try {
                            if (!response.isSuccessful()) {
                                Toast.makeText(requireActivity(), "Error al obtener datos: " + response.code(), Toast.LENGTH_LONG).show();
                                mostrarUsuarioVacio();
                                return;
                            }

                            JSONObject usuario = new JSONObject(body);
                            mostrarDatosUsuario(usuario);

                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(requireActivity(), "Error al parsear perfil", Toast.LENGTH_LONG).show();
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
        SharedPreferences prefs = requireActivity().getSharedPreferences("MyPrefs", requireActivity().MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();

        Intent intent = new Intent(requireActivity(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    private void mostrarUsuarioVacio() {
        saludoText.setText("¡Hola!");
        usernameText.setText("-");
        miembroDesdeText.setText("-");
        nivelText.setText("-");
        caloriasText.setText("0");
        diasActivosText.setText("0");
        rachaText.setText("Racha: 0 días 🔥");
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

        Volley.newRequestQueue(requireActivity()).add(request);
    }

    private void actualizarDiasActivos() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("ActivityPrefs", requireActivity().MODE_PRIVATE);
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
        ApiService.getRutinas(requireActivity(), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                requireActivity().runOnUiThread(() ->
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

                    requireActivity().runOnUiThread(() ->
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
