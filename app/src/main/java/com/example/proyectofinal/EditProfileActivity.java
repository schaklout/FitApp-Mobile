package com.example.proyectofinal;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private EditText etNombre, etPeso, etAltura;
    private Button btnGuardar;

    private String token;
    private int userId;
    private static final String BASE_URL_API = "https://iatic.es/ifc302/g1/fitapp/api.php/";

    private static final String BASE_URL =
            "https://iatic.es/ifc302/g1/fitapp/api.php/usuarios/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        etNombre = findViewById(R.id.etNombre);
        etPeso = findViewById(R.id.etPeso);
        etAltura = findViewById(R.id.etAltura);
        btnGuardar = findViewById(R.id.btnGuardar);

        token = TokenManager.getToken(this);
        userId = TokenManager.getUserId(this);

        if (token == null || userId == -1) {
            Toast.makeText(this, "Sesión inválida", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        cargarPerfil();

        btnGuardar.setOnClickListener(v -> guardarCambios());
    }


    private void cargarPerfil() {

        String url = BASE_URL_API  + "perfil";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    etNombre.setText(response.optString("nombre", ""));
                    etPeso.setText(response.optString("peso", ""));
                    etAltura.setText(response.optString("altura", ""));
                },
                error -> {
                    String msg = "Error al cargar perfil";
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        msg = "Código: " + error.networkResponse.statusCode + "\n" +
                                new String(error.networkResponse.data, StandardCharsets.UTF_8);
                    }
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }


    private void guardarCambios() {
        String url = BASE_URL_API + "perfil";

        JSONObject body = new JSONObject();
        try {
            body.put("nombre", etNombre.getText().toString().trim());
            body.put("peso", etPeso.getText().toString().trim());
            body.put("altura", etAltura.getText().toString().trim());
        } catch (Exception ignored) {}

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.PATCH,
                url,
                body,
                response -> {
                    Toast.makeText(this, "Perfil actualizado ✔", Toast.LENGTH_SHORT).show();
                    finish();
                },
                error -> {
                    String msg = "Error al guardar cambios";
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        msg = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                    }
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }
}
