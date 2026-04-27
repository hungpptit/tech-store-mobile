package com.example.tech_store_mobile.utils;

import android.os.Handler;
import android.os.Looper;

import com.example.tech_store_mobile.Model.CreateCardPaymentMethodRequest;
import com.example.tech_store_mobile.Model.CreateCardPaymentMethodResponse;
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

public class StripeCardApiClient {
    public interface Callback {
        void onSuccess(CreateCardPaymentMethodResponse response);
        void onError(String message);
    }

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Gson gson = new Gson();

    public void createCardPaymentMethod(CreateCardPaymentMethodRequest request, Callback callback) {
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

                CreateCardPaymentMethodResponse response = parseResponse(responseBody);
                if (response == null || response.getId() == null || response.getId().trim().isEmpty()) {
                    postError(callback, "API trả về thiếu paymentMethodId.");
                    return;
                }

                postSuccess(callback, response);
            } catch (Exception e) {
                postError(callback, e.getMessage() != null ? e.getMessage() : "Không thể tạo payment method.");
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    private HttpURLConnection openBackendConnection(CreateCardPaymentMethodRequest request) throws IOException {
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

    private CreateCardPaymentMethodResponse parseResponse(String responseBody) {
        if (responseBody == null || responseBody.trim().isEmpty()) {
            return null;
        }

        JsonElement element = JsonParser.parseString(responseBody);
        if (!element.isJsonObject()) {
            return null;
        }

        JsonObject object = element.getAsJsonObject();
        CreateCardPaymentMethodResponse response = new CreateCardPaymentMethodResponse();
        response.setId(readString(object, "id", "paymentMethodId", "payment_method_id"));
        response.setCustomerId(readString(object, "customerId", "customer_id"));
        response.setBrand(readString(object, "brand", "cardBrand", "card_brand"));
        response.setLast4(readString(object, "last4", "cardLast4", "card_last4"));
        response.setCardHolderName(readString(object, "cardHolderName", "card_holder_name"));
        response.setMessage(readString(object, "message", "error"));

        if (object.has("expMonth") && !object.get("expMonth").isJsonNull()) {
            response.setExpMonth(object.get("expMonth").getAsInt());
        } else if (object.has("exp_month") && !object.get("exp_month").isJsonNull()) {
            response.setExpMonth(object.get("exp_month").getAsInt());
        }

        if (object.has("expYear") && !object.get("expYear").isJsonNull()) {
            response.setExpYear(object.get("expYear").getAsInt());
        } else if (object.has("exp_year") && !object.get("exp_year").isJsonNull()) {
            response.setExpYear(object.get("exp_year").getAsInt());
        }

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

        String trimmedBody = responseBody.trim();
        if (trimmedBody.startsWith("<!DOCTYPE html") || trimmedBody.startsWith("<html") || trimmedBody.startsWith("<HTML")) {
            return "Backend đang trả về HTML thay vì JSON. Hãy kiểm tra lại BACKEND_BASE_URL, endpoint /api/payment-methods/create-card, hoặc trạng thái server/ngrok.";
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
        return baseUrl + "/api/payment-methods/create-card";
    }

    private void postSuccess(Callback callback, CreateCardPaymentMethodResponse response) {
        mainHandler.post(() -> callback.onSuccess(response));
    }

    private void postError(Callback callback, String message) {
        mainHandler.post(() -> callback.onError(message));
    }
}



