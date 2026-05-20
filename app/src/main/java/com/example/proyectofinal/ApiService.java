package com.example.proyectofinal;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import android.content.Context;
import android.content.SharedPreferences;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
public class ApiService {

    private static final String BASE_URL = BuildConfig.BASE_URL;
    private static void showToast(Context context, String mensaje) {
        new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(context.getApplicationContext(), mensaje, Toast.LENGTH_SHORT).show()
        );
    }


    private static Callback safeCallback(Context context, Callback externalCallback) {
        return new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                showToast(context, "Error de conexión con el servidor");
                if (externalCallback != null) externalCallback.onFailure(call, e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String bodyString = response.body() != null ? response.body().string() : "";

                if (!response.isSuccessful()) {
                    showToast(context, "Error del servidor (" + response.code() + ")");
                }

                try {
                    JSONObject json = new JSONObject(bodyString);

                    if (json.has("error")) {
                        showToast(context, json.getString("error"));
                        return;
                    }

                    if (json.has("token")) {
                        TokenManager.saveToken(context, json.getString("token"));
                        showToast(context, "Inicio de sesión exitoso");
                    }

                } catch (Exception ignored) {}

                if (externalCallback != null) {
                    Response newResponse = response.newBuilder()
                            .body(ResponseBody.create(
                                    bodyString,
                                    MediaType.parse("application/json; charset=utf-8")
                            ))
                            .build();

                    externalCallback.onResponse(call, newResponse);
                }
            }
        };
    }


    public static void login(Context context, String email, String password, Callback callback) {
        String json = "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
        ApiClient.post("/login", json, null, safeCallback(context, callback));
    }


    public static void getUsuarios(Context context, Callback callback) {
        String token = TokenManager.getToken(context);
        ApiClient.get("/usuarios", token, safeCallback(context, callback));
    }

    public static void getUsuarioById(Context context, int id, Callback callback) {
        String token = TokenManager.getToken(context);
        ApiClient.get("/usuarios/" + id, token, safeCallback(context, callback));
    }

    public static void crearUsuario(Context context, String nombre, String email, String password,
                                    double peso, double altura, int rolId, Callback callback) {
        String json = "{"
                + "\"nombre\":\"" + nombre + "\","
                + "\"email\":\"" + email + "\","
                + "\"password\":\"" + password + "\","
                + "\"peso\":" + peso + ","
                + "\"altura\":" + altura + ","
                + "\"rol_id\":" + rolId
                + "}";
        ApiClient.post("/usuarios", json, null, safeCallback(context, callback));
    }

    public static void updateUsuario(Context context, int id, String nombre, String email,
                                     String password, double peso, double altura, int rolId, Callback callback) {
        String token = TokenManager.getToken(context);
        String json = "{"
                + "\"nombre\":\"" + nombre + "\","
                + "\"email\":\"" + email + "\","
                + "\"password\":\"" + password + "\","
                + "\"peso\":" + peso + ","
                + "\"altura\":" + altura + ","
                + "\"rol_id\":" + rolId
                + "}";
        ApiClient.put("/usuarios/" + id, json, token, safeCallback(context, callback));
    }

    public static void deleteUsuario(Context context, int id, Callback callback) {
        String token = TokenManager.getToken(context);
        ApiClient.delete("/usuarios/" + id, null, token, safeCallback(context, callback));
    }

    public static void getDashboard1(Context context, Callback callback) {
        String token = TokenManager.getToken(context);
        ApiClient.get("/dashboard", token, safeCallback(context, callback));
    }

    public static void getProgreso1(Context context, Callback callback) {
        String token = TokenManager.getToken(context);
        ApiClient.get("/progreso", token, safeCallback(context, callback));
    }

    public static void getProgreso(Context context, Callback callback) {
        String token = TokenManager.getToken(context);
        if (token == null || token.isEmpty()) {
            showToast(context, "Usuario no autenticado");
            return;
        }
        ApiClient.get("/progreso", token, safeCallback(context, callback));
    }

    public static void crearProgreso(Context context, double peso, double imc,
                                     int calorias, int entrenamientos, Callback callback) {
        String token = TokenManager.getToken(context);
        String json = "{"
                + "\"peso\":" + peso + ","
                + "\"imc\":" + imc + ","
                + "\"calorias_quemadas\":" + calorias + ","
                + "\"entrenamientos_completados\":" + entrenamientos
                + "}";
        ApiClient.post("/progreso", json, token, safeCallback(context, callback));
    }

    public static void getRutinas(Context context, Callback callback) {
        String token = TokenManager.getToken(context);
        if (token == null || token.isEmpty()) {
            Toast.makeText(context, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }
        ApiClient.get("/rutinas_usuario", token, safeCallback(context, callback));
    }

    public static void crearRutina(Context context, String nombre, String descripcion, String nivel,
                                   String imagenUrl, String videoUrl, int tipoId, Callback callback) {
        String token = TokenManager.getToken(context);
        String json = "{"
                + "\"nombre\":\"" + nombre + "\","
                + "\"descripcion\":\"" + descripcion + "\","
                + "\"nivel\":\"" + nivel + "\","
                + "\"imagen_url\":\"" + imagenUrl + "\","
                + "\"video_url\":\"" + videoUrl + "\","
                + "\"tipo_id\":" + tipoId
                + "}";
        ApiClient.post("/rutinas", json, token, safeCallback(context, callback));
    }


    public static void getPerfil(Context context, Callback callback) {
        String token = TokenManager.getToken(context);
        ApiClient.get("/perfil", token, safeCallback(context, callback));
    }

    public static void finalizarRutina(Context context, int rutinaUsuarioId, Callback callback) {

        String url = "https://iatic.es/ifc302/g1/fitapp/api.php/rutinas_usuario/" + rutinaUsuarioId;

        SharedPreferences prefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("token", "");

        OkHttpClient client = new OkHttpClient();

        RequestBody body = RequestBody.create(
                "{\"completada\":1}",
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(url)
                .patch(body)
                .addHeader("Authorization", "Bearer " + token)
                .build();

        client.newCall(request).enqueue(callback);
    }


    public static void getTiposRutina(Context context, Callback callback) {

        String url = BASE_URL + "tipo_rutina";  // ajusta la URL según tu API
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(callback);
    }

    public static void getRutinasPorDia(Context context, int dia, Callback callback) {
        String token = TokenManager.getToken(context);
        if (token == null || token.isEmpty()) {
            showToast(context, "Usuario no autenticado");
            return;
        }
        // construye endpoint con parámetro dia
        ApiClient.get("/rutinas_por_dia?dia=" + dia, token, safeCallback(context, callback));
    }

    public static void guardarSesionEntrenamiento(Context context, int rutinaUsuarioId,
                                                  int tiempoMinutos, int calorias, String notas,
                                                  Callback callback) {
        String token = TokenManager.getToken(context);
        if (token == null || token.isEmpty()) {
            showToast(context, "Usuario no autenticado");
            return;
        }

        String json = "{"
                + "\"rutinas_usuario_id\":" + rutinaUsuarioId + ","
                + "\"tiempo_minutos\":" + tiempoMinutos + ","
                + "\"calorias_quemadas\":" + calorias + ","
                + "\"notas\":\"" + (notas != null ? notas.replace("\"", "\\\"") : "") + "\""
                + "}";

        ApiClient.post("/sesiones_entrenamiento", json, token, safeCallback(context, callback));
    }


    public static void getRutinasPorFecha(Context context, String fecha, Callback callback) {
        String token = TokenManager.getToken(context);
        if (token == null || token.isEmpty()) {
            showToast(context, "Usuario no autenticado");
            return;
        }
        // Endpoint que devuelve rutinas de una fecha específica
        ApiClient.get("/rutinas_usuario?fecha=" + fecha, token, safeCallback(context, callback));
    }

    public static void getRutinasCalendario(Context context, Callback callback) {
        String token = TokenManager.getToken(context);
        if (token == null || token.isEmpty()) {
            showToast(context, "Usuario no autenticado");
            return;
        }
        ApiClient.get("/rutinas_calendario", token, safeCallback(context, callback));
    }


    public static void getCarousel1(Context context, Callback callback) {
        String token = TokenManager.getToken(context);
        if (token == null || token.isEmpty()) {
            showToast(context, "Usuario no autenticado");
            return;
        }
        ApiClient.get("/carousel", token, safeCallback(context, callback));
    }

    public static void getCarousel(Context context, Callback callback) {
        String token = TokenManager.getToken(context);
        if (token == null || token.isEmpty()) {
            Toast.makeText(context, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }
        ApiClient.get("/carousel", token, safeCallback(context, callback));
    }

    public static void getDashboard(Context context, Callback callback) {
        String token = TokenManager.getToken(context);
        if (token == null || token.isEmpty()) {
            Toast.makeText(context, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }
        ApiClient.get("/dashboard", token, safeCallback(context, callback));
    }



}

