package com.example.proyectofinal;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import java.io.IOException;

public class RegisterActivity extends AppCompatActivity {

    private EditText nombreEditText, emailEditText, passwordEditText, pesoEditText, alturaEditText;
    private Button registerButton, backToLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        InsetsHelper.enable(this);
        setContentView(R.layout.activity_register);
        InsetsHelper.padBoth(findViewById(R.id.registerRoot));

        nombreEditText = findViewById(R.id.nombreEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        pesoEditText = findViewById(R.id.pesoEditText);
        alturaEditText = findViewById(R.id.alturaEditText);

        registerButton = findViewById(R.id.registerButton);
        backToLoginButton = findViewById(R.id.backToLoginButton);

        registerButton.setOnClickListener(v -> registrarUsuario());
        backToLoginButton.setOnClickListener(v -> finish());
    }

    private void registrarUsuario() {
        String nombre = nombreEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String pesoStr = pesoEditText.getText().toString().trim();
        String alturaStr = alturaEditText.getText().toString().trim();

        if (TextUtils.isEmpty(nombre) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(RegisterActivity.this, "Por favor completa todos los campos obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        double peso = 0;
        double altura = 0;
        try { peso = Double.parseDouble(pesoStr); } catch (Exception ignored) {}
        try { altura = Double.parseDouble(alturaStr); } catch (Exception ignored) {}

        ApiService.crearUsuario(this, nombre, email, password, peso, altura, 3, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(RegisterActivity.this, "Error al registrar: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                runOnUiThread(() -> {
                    if(response.isSuccessful()){
                        Toast.makeText(RegisterActivity.this, "Registro exitoso. Inicia sesión.", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(RegisterActivity.this, "Error al registrar: " + response.message(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }
}
