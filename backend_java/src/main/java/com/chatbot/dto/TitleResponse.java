package com.chatbot.dto;

public class TitleResponse {
    private String title;
    private String emoji;

    public TitleResponse() {}

    public TitleResponse(String title, String emoji) {
        this.title = title;
        this.emoji = emoji;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getEmoji() {
        return emoji;
    }

    public void setEmoji(String emoji) {
        this.emoji = emoji;
    }
}
