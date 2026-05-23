package com.example.demo.controller;

import com.example.demo.service.GeminiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*") // Tránh lỗi chặn CORS khi chạy local
public class ChatController {

    @Autowired
    private GeminiService geminiService;

    // SỬA TẠI ĐÂY: Thay đổi sang đón nhận chuỗi text thô trực tiếp gửi từ Frontend
    @PostMapping
    public String handleChat(@RequestBody String userMessage) {
        // Kiểm tra xem Frontend có gửi tin nhắn rỗng hay không
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return "Tin nhắn từ người dùng không được để trống!";
        }

        // Loại bỏ ký tự thừa hoặc dấu ngoặc kép bọc ngoài chuỗi text nếu có
        String cleanMessage = userMessage.trim().replaceAll("^\"|\"$", "");

        // Gửi sang GeminiService xử lý kết nối trực tiếp với Google
        return geminiService.askGemini(cleanMessage);
    }
}