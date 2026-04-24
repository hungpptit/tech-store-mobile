package com.example.tech_store_mobile.utils;

import android.os.Handler;
import android.os.Looper;
import com.example.tech_store_mobile.Model.CreatePaymentIntentRequest;
import com.example.tech_store_mobile.Model.CreatePaymentIntentResponse;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StripePaymentApiClient {
    public interface Callback {
        void onSuccess(CreatePaymentIntentResponse response);
        void onError(String message);
    }

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Gson gson = new Gson();

    public void createPaymentIntent(CreatePaymentIntentRequest request, Callback callback) {
        executorService.execute(() -> {
            HttpURLConnection connection = null;
            try {
                if (!StripeConfig.isConfigured()) {
                    postError(callback, "Stripe chưa được cấu hình. Hãy điền backend URL và publishable key.");
                    return;
                }

                connection = openBackendConnection(request);

                int code = connection.getResponseCode();
                String responseBody = readStream(code >= 200 && code < 300
                        ? connection.getInputStream()
                        : connection.getErrorStream());

                if (code < 200 || code >= 300) {
                    postError(callback, extractErrorMessage(responseBody, code));
                    return;
                }

                CreatePaymentIntentResponse response = parseResponse(responseBody);
                if (response == null || response.getClientSecret() == null || response.getClientSecret().trim().isEmpty()) {
                    postError(callback, "API trả về thiếu clientSecret.");
                    return;
                }

                postSuccess(callback, response);
            } catch (Exception e) {
                postError(callback, e.getMessage() != null ? e.getMessage() : "Không thể tạo payment intent.");
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    private HttpURLConnection openBackendConnection(CreatePaymentIntentRequest request) throws IOException {
        URL url = new URL(buildEndpointUrl());
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(15000);
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("ngrok-skip-browser-warning", "true");

        String body = gson.toJson(request);
        try (OutputStream outputStream = connection.getOutputStream()) {
            outputStream.write(body.getBytes(StandardCharsets.UTF_8));
        }
        return connection;
    }

    private CreatePaymentIntentResponse parseResponse(String responseBody) {
        if (responseBody == null || responseBody.trim().isEmpty()) {
            return null;
        }

        JsonElement element = JsonParser.parseString(responseBody);
        if (!element.isJsonObject()) {
            return null;
        }

        JsonObject object = element.getAsJsonObject();
        CreatePaymentIntentResponse response = new CreatePaymentIntentResponse();
        response.setClientSecret(readString(object, "clientSecret", "client_secret"));
        response.setPaymentIntentId(readString(object, "paymentIntentId", "payment_intent_id"));
        response.setStatus(readString(object, "status"));
        response.setMessage(readString(object, "message", "error"));
        return response;
    }

    private String readString(JsonObject object, String... keys) {
        for (String key : keys) {
            if (object.has(key) && !object.get(key).isJsonNull()) {
                return object.get(key).getAsString();
            }
        }
        return null;
    }

    private String readStream(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return null;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            return builder.toString();
        }
    }

    private String extractErrorMessage(String responseBody, int code) {
        if (responseBody == null || responseBody.trim().isEmpty()) {
            return "API error HTTP " + code;
        }

        try {
            JsonElement element = JsonParser.parseString(responseBody);
            if (element.isJsonObject()) {
                JsonObject object = element.getAsJsonObject();
                String message = readString(object, "message", "error", "detail");
                if (message != null && !message.trim().isEmpty()) {
                    return message;
                }
            }
        } catch (Exception ignored) {
        }

        return responseBody;
    }

    private String buildEndpointUrl() {
        String baseUrl = StripeConfig.BACKEND_BASE_URL;
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl + StripeConfig.CREATE_PAYMENT_INTENT_ENDPOINT;
    }

    private void postSuccess(Callback callback, CreatePaymentIntentResponse response) {
        mainHandler.post(() -> callback.onSuccess(response));
    }

    private void postError(Callback callback, String message) {
        mainHandler.post(() -> callback.onError(message));
    }
}







