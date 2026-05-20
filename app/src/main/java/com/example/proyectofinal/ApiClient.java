package com.example.proyectofinal;

import androidx.annotation.Nullable;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class ApiClient {    

    private static final String BASE_URL = BuildConfig.BASE_URL;




    private static final OkHttpClient client = new OkHttpClient();
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private static String buildUrl1(String endpoint) {
        if (!endpoint.startsWith("/")) {
            endpoint = "/" + endpoint;
        }
        return BASE_URL + endpoint;
    }

    private static String buildUrl(String endpoint) {
        if (endpoint.startsWith("/")) {
            endpoint = endpoint.substring(1);
        }
        String finalUrl = BASE_URL + "/" + endpoint;
        return finalUrl;
    }

    // ==========================
    //   METODOS HTTP GENERALES
    // ==========================

    public static void get(String endpoint, @Nullable String token, Callback callback) {

        Request.Builder builder = new Request.Builder()
                .url(buildUrl(endpoint))
                .get();

        if (token != null) {
            builder.addHeader("Authorization", "Bearer " + token);
        }

        Request request = builder.build();
        client.newCall(request).enqueue(callback);
    }


    public static void post(String endpoint, String json, @Nullable String token, Callback callback) {

        RequestBody body = RequestBody.create(json, JSON);

        Request.Builder builder = new Request.Builder()
                .url(buildUrl(endpoint))
                .post(body);

        if (token != null) {
            builder.addHeader("Authorization", "Bearer " + token);
        }

        Request request = builder.build();
        client.newCall(request).enqueue(callback);
    }


    public static void put(String endpoint, String json, @Nullable String token, Callback callback) {

        RequestBody body = RequestBody.create(json, JSON);

        Request.Builder builder = new Request.Builder()
                .url(buildUrl(endpoint))
                .put(body);

        if (token != null) {
            builder.addHeader("Authorization", "Bearer " + token);
        }

        Request request = builder.build();
        client.newCall(request).enqueue(callback);
    }


    public static void delete(String endpoint, @Nullable String json, @Nullable String token, Callback callback) {

        Request.Builder builder;

        if (json != null) {
            RequestBody body = RequestBody.create(json, JSON);
            builder = new Request.Builder()
                    .url(buildUrl(endpoint))
                    .delete(body);
        } else {
            builder = new Request.Builder()
                    .url(buildUrl(endpoint))
                    .delete();
        }

        if (token != null) {
            builder.addHeader("Authorization", "Bearer " + token);
        }

        Request request = builder.build();
        client.newCall(request).enqueue(callback);
    }
}
