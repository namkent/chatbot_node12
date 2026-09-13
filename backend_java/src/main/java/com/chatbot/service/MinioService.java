package com.chatbot.service;

import com.chatbot.config.AppProperties;
import com.chatbot.dto.UploadResponse;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class MinioService {

    private static final Logger log = LoggerFactory.getLogger(MinioService.class);
    private static final Pattern DATA_URI_PATTERN = Pattern.compile("^data:([a-zA-Z0-9]+/[a-zA-Z0-9\\-+.]+);base64,(.+)$");

    private final AppProperties appProperties;
    private OkHttpClient httpClient;

    public MinioService(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @PostConstruct
    public void init() {
        this.httpClient = new OkHttpClient.Builder().build();
    }

    /**
     * Upload ảnh base64 trực tiếp lên MinIO bucket
     */
    public UploadResponse uploadBase64(String rawData, String origName, String customMimeType) throws IOException {
        if (rawData == null || rawData.trim().isEmpty()) {
            throw new IllegalArgumentException("Tham số ảnh (chuỗi base64) là bắt buộc.");
        }

        String mimeType = "image/png";
        String cleanBase64 = rawData.trim();

        Matcher matcher = DATA_URI_PATTERN.matcher(cleanBase64);
        if (matcher.find()) {
            mimeType = matcher.group(1).toLowerCase();
            cleanBase64 = matcher.group(2);
        } else if (customMimeType != null && !customMimeType.trim().isEmpty()) {
            mimeType = customMimeType.trim().toLowerCase();
        }

        String ext = "png";
        if (mimeType.contains("jpeg") || mimeType.contains("jpg")) ext = "jpg";
        else if (mimeType.contains("webp")) ext = "webp";
        else if (mimeType.contains("gif")) ext = "gif";
        else if (mimeType.contains("svg")) ext = "svg";

        String safeOrigName = "";
        if (origName != null && !origName.trim().isEmpty()) {
            safeOrigName = origName.replaceAll("[^a-zA-Z0-9_\\-.]", "_").replaceAll("\\.[^.]+$", "");
            if (safeOrigName.length() > 30) {
                safeOrigName = safeOrigName.substring(0, 30);
            }
        }

        String uniqueSuffix = System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 5);
        String filename = (safeOrigName.isEmpty() ? "img_" : (safeOrigName + "_")) + uniqueSuffix + "." + ext;

        byte[] imageBytes = Base64.getDecoder().decode(cleanBase64.replaceAll("\\s+", ""));

        // Gọi HTTP PUT lên MinIO
        String minioEndpoint = appProperties.getMinio().getEndpoint().replaceAll("/+$", "");
        String bucket = appProperties.getMinio().getBucket().trim();
        String encodedKey = URLEncoder.encode(filename, "UTF-8").replace("+", "%20");
        String targetUrl = minioEndpoint + "/" + bucket + "/" + encodedKey;

        RequestBody requestBody = RequestBody.create(MediaType.parse(mimeType), imageBytes);
        Request request = new Request.Builder()
                .url(targetUrl)
                .put(requestBody)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "";
                log.error("[MinIO Upload] Thất bại với HTTP status {}: {}", response.code(), errorBody);
                throw new IOException("MinIO PUT failed status " + response.code() + ": " + errorBody);
            }

            String publicUrl = appProperties.getMinio().getPublicUrl().replaceAll("/+$", "") + "/" + encodedKey;
            log.info("[MinIO Upload] Đã lưu ảnh key={} ({} bytes) URL={}", filename, imageBytes.length, publicUrl);

            return UploadResponse.ok(
                    filename,
                    publicUrl,
                    mimeType,
                    imageBytes.length,
                    "data:" + mimeType + ";base64," + cleanBase64
            );
        }
    }

    /**
     * Xóa ảnh khỏi MinIO bucket theo key
     */
    public UploadResponse delete(String key) throws IOException {
        String cleanKey = key != null ? key.replaceAll("^.*[\\\\/]", "").trim() : "";
        if (cleanKey.isEmpty()) {
            return UploadResponse.okDelete("");
        }

        String minioEndpoint = appProperties.getMinio().getEndpoint().replaceAll("/+$", "");
        String bucket = appProperties.getMinio().getBucket().trim();
        String encodedKey = URLEncoder.encode(cleanKey, "UTF-8").replace("+", "%20");
        String targetUrl = minioEndpoint + "/" + bucket + "/" + encodedKey;

        Request request = new Request.Builder()
                .url(targetUrl)
                .delete()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            // Status 200, 204 hoặc 404 (file đã xóa hoặc không tồn tại) đều xem là thành công
            if (response.isSuccessful() || response.code() == 404) {
                log.info("[MinIO Delete] Đã xóa ảnh key={}", cleanKey);
                return UploadResponse.okDelete(cleanKey);
            } else {
                String errorBody = response.body() != null ? response.body().string() : "";
                throw new IOException("MinIO DELETE failed status " + response.code() + ": " + errorBody);
            }
        }
    }
}
