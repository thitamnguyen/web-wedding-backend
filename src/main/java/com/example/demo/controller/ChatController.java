package com.example.demo.controller;

import com.example.demo.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/chatbot")
@CrossOrigin(origins = "*")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @PostMapping("/ask")
    public ResponseEntity<?> askChatbot(@RequestBody Map<String, String> request) {
        try {
            String userMessage = request.get("message");
            if (userMessage == null || userMessage.trim().isEmpty()) {
                return ResponseEntity.ok(Map.of("reply", "Hai bạn cần mình tư vấn thông tin gì ạ?"));
            }

            Map<String, Object> result = chatService.getAIResponse(userMessage);

            // Bẫy lỗi đường truyền mạng hoặc Token OpenRouter gặp sự cố
            if (result == null || result.containsKey("error")) {
                return ResponseEntity.ok(Map.of("reply", "LuxeAI đang kiểm tra lại lịch chụp một chút, hai bạn nhắn lại sau vài giây nhé!"));
            }

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("reply", "Lỗi hệ thống máy chủ: " + e.getMessage()));
        }
    }
}