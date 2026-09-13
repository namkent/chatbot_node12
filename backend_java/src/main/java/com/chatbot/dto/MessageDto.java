package com.chatbot.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageDto {
    private String role;
    private Object content; // String hoặc List (cho multimodal vision)

    @JsonProperty("tool_calls")
    private Object toolCalls;

    @JsonProperty("tool_call_id")
    private String toolCallId;

    private String name;

    public MessageDto() {}

    public MessageDto(String role, Object content) {
        this.role = role;
        this.content = content;
    }

    public MessageDto(String role, Object content, String toolCallId, String name) {
        this.role = role;
        this.content = content;
        this.toolCallId = toolCallId;
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }

    public Object getToolCalls() {
        return toolCalls;
    }

    public void setToolCalls(Object toolCalls) {
        this.toolCalls = toolCalls;
    }

    public String getToolCallId() {
        return toolCallId;
    }

    public void setToolCallId(String toolCallId) {
        this.toolCallId = toolCallId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
