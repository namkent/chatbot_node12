package com.chatbot.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UploadRequest {
    private String image;
    private String base64;
    private String data;
    private String name;
    private String mimeType;
    private String key;
    private String url;

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getBase64() {
        return base64;
    }

    public void setBase64(String base64) {
        this.base64 = base64;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
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

    /**
     * Lấy chuỗi raw base64 từ bất kỳ thuộc tính nào được gửi lên
     */
    public String getEffectiveRawData() {
        if (image != null && !image.trim().isEmpty()) return image.trim();
        if (base64 != null && !base64.trim().isEmpty()) return base64.trim();
        if (data != null && !data.trim().isEmpty()) return data.trim();
        return null;
    }
}
