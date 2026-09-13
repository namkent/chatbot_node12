package com.chatbot.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SessionDetailDto extends SessionDto {
    private String systemPrompt;
    private List<Map<String, Object>> messageList;
    private List<Map<String, Object>> apiMessagesHistory;

    public SessionDetailDto() {
        super();
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

    public List<Map<String, Object>> getMessageList() {
        return messageList;
    }

    public void setMessageList(List<Map<String, Object>> messageList) {
        this.messageList = messageList;
    }

    public List<Map<String, Object>> getApiMessagesHistory() {
        return apiMessagesHistory;
    }

    public void setApiMessagesHistory(List<Map<String, Object>> apiMessagesHistory) {
        this.apiMessagesHistory = apiMessagesHistory;
    }
}
