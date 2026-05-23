package com.example.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    // API Key chuẩn của bạn từ hệ thống Google AI Studio
    private final String apiKey = "AIzaSyDSmoiYs9J5rC6vVAuOhwv1m7K3ZjU5FQk";

    // Sử dụng đúng Endpoint v1beta dành cho phân vùng tài khoản miễn phí (Free Tier)
    private final String apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey;

    public String askGemini(String userMessage) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Xây dựng cấu trúc JSON Payload theo đúng quy định nghiêm ngặt của Google
            Map<String, Object> textMap = new HashMap<>();
            textMap.put("text", userMessage);

            List<Map<String, Object>> partsList = new ArrayList<>();
            partsList.add(textMap);

            Map<String, Object> contentMap = new HashMap<>();
            contentMap.put("parts", partsList);

            List<Map<String, Object>> contentsList = new ArrayList<>();
            contentsList.add(contentMap);

            Map<String, Object> body = new HashMap<>();
            body.put("contents", contentsList);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            // Gửi dữ liệu đồng bộ tới server Google
            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, entity, Map.class);
            Map<String, Object> responseBody = response.getBody();

            // Tiến hành bóc tách chuỗi dữ liệu Text trả về từ luồng JSON
            if (response.getStatusCode().is2xxSuccessful() && responseBody != null) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");
                if (candidates != null && !candidates.isEmpty()) {
                    Map<String, Object> firstCandidate = candidates.get(0);
                    Map<String, Object> content = (Map<String, Object>) firstCandidate.get("content");
                    if (content != null) {
                        List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                        if (parts != null && !parts.isEmpty()) {
                            return (String) parts.get(0).get("text");
                        }
                    }
                }
            }
            return "Phản hồi từ Google không đúng cấu trúc văn bản.";

        } catch (Exception e) {
            e.printStackTrace();
            return "Lỗi kết nối API: " + e.getMessage();
        }
    }
}