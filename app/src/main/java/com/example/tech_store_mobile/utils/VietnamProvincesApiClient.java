package com.example.tech_store_mobile.utils;

import android.os.Handler;
import android.os.Looper;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.util.Log;

public class VietnamProvincesApiClient {
    private static final String BASE_URL = "https://provinces.open-api.vn/api";

    public interface Callback<T> {
        void onSuccess(T data);
        void onError(String message);
    }

    public static class ProvinceOption {
        private String code;
        private String name;

        public ProvinceOption() {
        }

        public ProvinceOption(String code, String name) {
            this.code = code;
            this.name = name;
        }

        public String getCode() {
            return code;
        }

        public String getName() {
            return name;
        }
    }

    public static class WardOption {
        private String code;
        private String name;

        public WardOption() {
        }

        public WardOption(String code, String name) {
            this.code = code;
            this.name = name;
        }

        public String getCode() {
            return code;
        }

        public String getName() {
            return name;
        }
    }

    public static class DistrictOption {
        private String code;
        private String name;
        private final List<WardOption> wards = new ArrayList<>();

        public DistrictOption() {
        }

        public DistrictOption(String code, String name) {
            this.code = code;
            this.name = name;
        }

        public String getCode() {
            return code;
        }

        public String getName() {
            return name;
        }

        public List<WardOption> getWards() {
            return wards;
        }
    }

    public static class ProvinceDetail {
        private String code;
        private String name;
        private final List<DistrictOption> districts = new ArrayList<>();

        public ProvinceDetail() {
        }

        public String getCode() {
            return code;
        }

        public String getName() {
            return name;
        }

        public List<DistrictOption> getDistricts() {
            return districts;
        }
    }

    public static class DistrictDetail {
        private String code;
        private String name;
        private final List<WardOption> wards = new ArrayList<>();

        public DistrictDetail() {
        }

        public String getCode() {
            return code;
        }

        public String getName() {
            return name;
        }

        public List<WardOption> getWards() {
            return wards;
        }
    }

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Gson gson = new Gson();

    public void fetchProvinces(Callback<List<ProvinceOption>> callback) {
        executorService.execute(() -> {
            HttpURLConnection connection = null;
            try {
                connection = openConnection(BASE_URL + "/p/");
                int code = connection.getResponseCode();
                String body = readStream(code >= 200 && code < 300 ? connection.getInputStream() : connection.getErrorStream());

                if (code < 200 || code >= 300) {
                    postError(callback, extractErrorMessage(body, code));
                    return;
                }

                List<ProvinceOption> provinces = parseProvinces(body);
                postSuccess(callback, provinces);
            } catch (Exception e) {
                postError(callback, e.getMessage() != null ? e.getMessage() : "Không thể tải danh sách tỉnh/thành.");
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    public void fetchProvinceDetail(String provinceCode, Callback<ProvinceDetail> callback) {
        executorService.execute(() -> {
            HttpURLConnection connection = null;
            try {
                String safeCode = provinceCode == null ? "" : provinceCode.trim();
                if (safeCode.isEmpty()) {
                    postError(callback, "Mã tỉnh/thành không hợp lệ.");
                    return;
                }

                String url = BASE_URL + "/p/" + safeCode + "?depth=2";
                connection = openConnection(url);
                int httpCode = connection.getResponseCode();
                String body = readStream(httpCode >= 200 && httpCode < 300 ? connection.getInputStream() : connection.getErrorStream());

                if (httpCode < 200 || httpCode >= 300) {
                    postError(callback, extractErrorMessage(body, httpCode));
                    return;
                }

                ProvinceDetail detail = parseProvinceDetail(body);
                postSuccess(callback, detail);
            } catch (Exception e) {
                postError(callback, e.getMessage() != null ? e.getMessage() : "Không thể tải dữ liệu địa chỉ.");
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    public void fetchDistrictDetail(String districtCode, Callback<DistrictDetail> callback) {
        executorService.execute(() -> {
            HttpURLConnection connection = null;
            try {
                String safeCode = districtCode == null ? "" : districtCode.trim();
                if (safeCode.isEmpty()) {
                    postError(callback, "Mã quận/huyện không hợp lệ.");
                    return;
                }

                String url = BASE_URL + "/d/" + safeCode + "?depth=2";
                connection = openConnection(url);
                int httpCode = connection.getResponseCode();
                String body = readStream(httpCode >= 200 && httpCode < 300 ? connection.getInputStream() : connection.getErrorStream());

                if (httpCode < 200 || httpCode >= 300) {
                    postError(callback, extractErrorMessage(body, httpCode));
                    return;
                }

                DistrictDetail detail = parseDistrictDetail(body);
                postSuccess(callback, detail);
            } catch (Exception e) {
                postError(callback, e.getMessage() != null ? e.getMessage() : "Không thể tải dữ liệu quận/huyện.");
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    public void fetchDistrictWards(String districtCode, Callback<List<WardOption>> callback) {
        executorService.execute(() -> {
            HttpURLConnection connection = null;
            try {
                String safeCode = districtCode == null ? "" : districtCode.trim();
                if (safeCode.isEmpty()) {
                    postError(callback, "Mã quận/huyện không hợp lệ.");
                    return;
                }

                String url = BASE_URL + "/d/" + safeCode + "?depth=2";
                connection = openConnection(url);
                int httpCode = connection.getResponseCode();
                String body = readStream(httpCode >= 200 && httpCode < 300 ? connection.getInputStream() : connection.getErrorStream());

                if (httpCode < 200 || httpCode >= 300) {
                    postError(callback, extractErrorMessage(body, httpCode));
                    return;
                }

                List<WardOption> wards = parseWards(body);
                postSuccess(callback, wards);
            } catch (Exception e) {
                postError(callback, e.getMessage() != null ? e.getMessage() : "Không thể tải dữ liệu phường/xã.");
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    private HttpURLConnection openConnection(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(15000);
        connection.setRequestProperty("Accept", "application/json");
        return connection;
    }

    private List<ProvinceOption> parseProvinces(String body) {
        List<ProvinceOption> provinces = new ArrayList<>();
        JsonElement root = JsonParser.parseString(body);
        if (!root.isJsonArray()) {
            return provinces;
        }

        JsonArray array = root.getAsJsonArray();
        for (JsonElement element : array) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject object = element.getAsJsonObject();
            provinces.add(new ProvinceOption(readString(object, "code"), readString(object, "name")));
        }
        return provinces;
    }

    private ProvinceDetail parseProvinceDetail(String body) {
        JsonObject object = JsonParser.parseString(body).getAsJsonObject();
        ProvinceDetail detail = new ProvinceDetail();
        detail.code = readString(object, "code");
        detail.name = readString(object, "name");
        Log.d("ProvinceAPI", "Parsing province: " + detail.name);

        JsonArray districtsArray = object.has("districts") && object.get("districts").isJsonArray()
                ? object.getAsJsonArray("districts")
                : new JsonArray();

        for (JsonElement districtElement : districtsArray) {
            if (!districtElement.isJsonObject()) {
                continue;
            }

            JsonObject districtObject = districtElement.getAsJsonObject();
            DistrictOption district = new DistrictOption(readString(districtObject, "code"), readString(districtObject, "name"));

            // Parse wards for this district
            JsonArray wardsArray = districtObject.has("wards") && districtObject.get("wards").isJsonArray()
                    ? districtObject.getAsJsonArray("wards")
                    : new JsonArray();

            for (JsonElement wardElement : wardsArray) {
                if (!wardElement.isJsonObject()) {
                    continue;
                }
                JsonObject wardObject = wardElement.getAsJsonObject();
                WardOption ward = new WardOption(readString(wardObject, "code"), readString(wardObject, "name"));
                district.getWards().add(ward);
            }

            detail.getDistricts().add(district);
        }

        return detail;
    }

    private DistrictDetail parseDistrictDetail(String body) {
        JsonObject object = JsonParser.parseString(body).getAsJsonObject();
        DistrictDetail detail = new DistrictDetail();
        detail.code = readString(object, "code");
        detail.name = readString(object, "name");
        Log.d("DistrictAPI", "Parsing district: " + detail.name);

        JsonArray wardsArray = object.has("wards") && object.get("wards").isJsonArray()
                ? object.getAsJsonArray("wards")
                : new JsonArray();

        for (JsonElement wardElement : wardsArray) {
            if (!wardElement.isJsonObject()) {
                continue;
            }
            JsonObject wardObject = wardElement.getAsJsonObject();
            WardOption ward = new WardOption(readString(wardObject, "code"), readString(wardObject, "name"));
            detail.getWards().add(ward);
        }

        return detail;
    }

    private List<WardOption> parseWards(String body) {
        List<WardOption> wards = new ArrayList<>();
        JsonObject object = JsonParser.parseString(body).getAsJsonObject();

        JsonArray wardsArray = object.has("wards") && object.get("wards").isJsonArray()
                ? object.getAsJsonArray("wards")
                : new JsonArray();

        for (JsonElement wardElement : wardsArray) {
            if (!wardElement.isJsonObject()) {
                continue;
            }
            JsonObject wardObject = wardElement.getAsJsonObject();
            wards.add(new WardOption(readString(wardObject, "code"), readString(wardObject, "name")));
        }

        return wards;
    }

    private String readString(JsonObject object, String key) {
        if (object == null || key == null || !object.has(key) || object.get(key).isJsonNull()) {
            return "";
        }
        JsonElement element = object.get(key);
        if (element.isJsonPrimitive()) {
            if (element.getAsJsonPrimitive().isNumber()) {
                return element.getAsNumber().toString();
            }
            return element.getAsString();
        }
        return gson.toJson(element);
    }

    private String readStream(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        }
        return builder.toString();
    }

    private String extractErrorMessage(String body, int code) {
        if (body == null || body.trim().isEmpty()) {
            return "API provinces trả về lỗi (" + code + ").";
        }
        return body;
    }

    private <T> void postSuccess(Callback<T> callback, T data) {
        if (callback == null) {
            return;
        }
        mainHandler.post(() -> callback.onSuccess(data));
    }

    private void postError(Callback<?> callback, String message) {
        if (callback == null) {
            return;
        }
        mainHandler.post(() -> callback.onError(message));
    }
}












