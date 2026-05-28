package com.example.proyectofinal;

import static android.app.ProgressDialog.show;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.*;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    EditText emailEditText, passwordEditText;
    Button loginButton;
    TextView registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!TokenManager.isOnboardingCompleted(this)) {
            startActivity(new Intent(this, OnboardingActivity.class));
            finish();
            return;
        }

        InsetsHelper.enable(this);
        setContentView(R.layout.activity_main);
        InsetsHelper.padBoth(findViewById(R.id.mainRoot));

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Por favor ingresa email y contraseña", Toast.LENGTH_SHORT).show();
                    return;
                }

                ApiService.login(MainActivity.this, email, password, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        runOnUiThread(() ->
                                Toast.makeText(MainActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show()
                        );
                    }

                    @Override
                    public void onResponse(Call call, Response response) {
                        if (response.body() == null) {
                            runOnUiThread(() ->
                                    Toast.makeText(MainActivity.this, "Respuesta vacía del servidor", Toast.LENGTH_SHORT).show()
                            );
                            return;
                        }
                        String bodyString = "";
                        try {
                            bodyString = response.body().string();
                        } catch (Exception e) {
                            runOnUiThread(() ->
                                    Toast.makeText(MainActivity.this, "Error al procesar respuesta", Toast.LENGTH_SHORT).show()
                            );
                            return;
                        }
                        try {
                            JSONObject jsonObject = new JSONObject(bodyString);

                            if (jsonObject.has("error")) {
                                runOnUiThread(() ->
                                        Toast.makeText(MainActivity.this, jsonObject.optString("error", "Error desconocido"), Toast.LENGTH_SHORT).show()
                                );
                                return;
                            }

                            String token = jsonObject.getString("token");
                            JSONObject user = jsonObject.getJSONObject("user");
                            String user_id = user.getString("id");
                            String nombre = user.getString("nombre");
                            String email = user.getString("email");
                            String pesoObj = user.getString("peso");
                            String alturaObj = user.getString("altura");

                            TokenManager.saveToken(MainActivity.this, token);
                            TokenManager.saveUsuario(MainActivity.this, user_id, nombre, pesoObj, alturaObj, email);

                            runOnUiThread(() -> {
                                Intent intent = new Intent(MainActivity.this, MainContainerActivity.class);
                                startActivity(intent);
                                finish();
                            });

                        } catch (JSONException e) {
                            runOnUiThread(() ->
                                    Toast.makeText(MainActivity.this, "Error al procesar respuesta", Toast.LENGTH_SHORT).show()
                            );
                        } finally {
                            response.close();
                        }
                    }
                });
            }
        });


        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }
}