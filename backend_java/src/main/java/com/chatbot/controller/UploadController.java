package com.chatbot.controller;

import com.chatbot.dto.UploadRequest;
import com.chatbot.dto.UploadResponse;
import com.chatbot.service.MinioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api")
public class UploadController {

    private final MinioService minioService;

    public UploadController(MinioService minioService) {
        this.minioService = minioService;
    }

    /**
     * Endpoint Upload ảnh lên MinIO S3: POST /api/upload
     */
    @PostMapping("/upload")
    public ResponseEntity<UploadResponse> uploadImage(@RequestBody UploadRequest request) {
        String rawData = request.getEffectiveRawData();
        if (rawData == null || rawData.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(UploadResponse.fail("Tham số `image` (chuỗi base64) là bắt buộc."));
        }

        try {
            UploadResponse response = minioService.uploadBase64(rawData, request.getName(), request.getMimeType());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(UploadResponse.fail(ex.getMessage()));
        } catch (IOException ex) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(UploadResponse.fail("Không thể tải ảnh lên MinIO: " + ex.getMessage()));
        }
    }

    /**
     * Endpoint Xóa ảnh khỏi MinIO: DELETE /api/upload
     */
    @DeleteMapping("/upload")
    public ResponseEntity<UploadResponse> deleteImage(
            @RequestParam(value = "key", required = false) String paramKey,
            @RequestBody(required = false) UploadRequest bodyRequest) {
        String key = (paramKey != null && !paramKey.trim().isEmpty())
                ? paramKey.trim()
                : (bodyRequest != null ? (bodyRequest.getKey() != null ? bodyRequest.getKey() : bodyRequest.getUrl()) : null);

        return processDelete(key);
    }

    /**
     * Endpoint Xóa ảnh dự phòng: POST /api/upload/delete
     */
    @PostMapping("/upload/delete")
    public ResponseEntity<UploadResponse> postDeleteUpload(
            @RequestParam(value = "key", required = false) String paramKey,
            @RequestBody(required = false) UploadRequest bodyRequest) {
        String key = (paramKey != null && !paramKey.trim().isEmpty())
                ? paramKey.trim()
                : (bodyRequest != null ? (bodyRequest.getKey() != null ? bodyRequest.getKey() : bodyRequest.getUrl()) : null);

        return processDelete(key);
    }

    /**
     * Endpoint Xóa ảnh dự phòng: POST /api/delete
     */
    @PostMapping("/delete")
    public ResponseEntity<UploadResponse> postDelete(
            @RequestParam(value = "key", required = false) String paramKey,
            @RequestBody(required = false) UploadRequest bodyRequest) {
        String key = (paramKey != null && !paramKey.trim().isEmpty())
                ? paramKey.trim()
                : (bodyRequest != null ? (bodyRequest.getKey() != null ? bodyRequest.getKey() : bodyRequest.getUrl()) : null);

        return processDelete(key);
    }

    private ResponseEntity<UploadResponse> processDelete(String key) {
        if (key == null || key.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(UploadResponse.fail("Tham số `key` cần xóa là bắt buộc."));
        }

        try {
            UploadResponse response = minioService.delete(key);
            return ResponseEntity.ok(response);
        } catch (IOException ex) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(UploadResponse.fail("Không thể xóa ảnh từ MinIO: " + ex.getMessage()));
        }
    }
}
