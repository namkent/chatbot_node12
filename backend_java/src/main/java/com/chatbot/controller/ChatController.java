package com.chatbot.controller;

import com.chatbot.dto.ChatRequest;
import com.chatbot.service.AiChatService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final AiChatService aiChatService;

    public ChatController(AiChatService aiChatService) {
        this.aiChatService = aiChatService;
    }

    /**
     * Endpoint Chat Proxy Streaming: POST /api/chat
     * Hỗ trợ Server-Sent Events (SSE), Multimodal Vision, Reasoning/Thinking và gợi ý câu hỏi tiếp theo
     */
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public void chatStream(@RequestBody ChatRequest request, HttpServletResponse response) throws IOException {
        aiChatService.streamChat(request, response);
    }
}
