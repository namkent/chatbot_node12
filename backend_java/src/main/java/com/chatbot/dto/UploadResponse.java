package com.chatbot.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UploadResponse {
    private boolean success;
    private String key;
    private String url;
    private String mimeType;
    private Long size;
    private String base64;
    private String error;

    public UploadResponse() {}

    public static UploadResponse ok(String key, String url, String mimeType, long size, String base64) {
        UploadResponse res = new UploadResponse();
        res.success = true;
        res.key = key;
        res.url = url;
        res.mimeType = mimeType;
        res.size = size;
        res.base64 = base64;
        return res;
    }

    public static UploadResponse okDelete(String key) {
        UploadResponse res = new UploadResponse();
        res.success = true;
        res.key = key;
        return res;
    }

    public static UploadResponse fail(String error) {
        UploadResponse res = new UploadResponse();
        res.success = false;
        res.error = error;
        return res;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getBase64() {
        return base64;
    }

    public void setBase64(String base64) {
        this.base64 = base64;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
