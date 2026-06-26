package com.example.demo.controller;

import com.example.demo.dto.AiResponse;
import com.example.demo.dto.TryOnResult;
import com.example.demo.model.WeddingDress;
import com.example.demo.repository.WeddingDressRepository;
import com.example.demo.service.AiService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:5174"}) // Cho phép React truy cập
public class AIRecommendController {

    @Autowired
    private AiService aiService;

    @Autowired
    private WeddingDressRepository
            weddingDressRepository;

    @PostMapping("/recommend")
    public ResponseEntity<?> recommendDress(

            @RequestParam("file") MultipartFile file,

            @RequestParam(value = "style", required = false)
            String style,

            @RequestParam(value = "budget", required = false)
            Long budget

    ) throws IOException {

        try {

            // gọi AI
            AiResponse aiResult = aiService.predictBodyShape(file);

            String bodyShape = aiResult.getBody_shape();

            System.out.println("Detected body shape: " + bodyShape);

            List<WeddingDress> dresses;

            if (style == null && budget == null) {
                dresses = weddingDressRepository.findByBodyShape(bodyShape);
            } else {
                dresses = weddingDressRepository.findRecommendedDress(
                        bodyShape,
                        style,
                        budget
                );
            }

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", true);
            response.put("bodyShape", bodyShape);
            response.put("message", dresses.isEmpty() ? "Không có váy phù hợp" : null);
            response.put("dresses", dresses);

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            Map<String, Object> error = new LinkedHashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());

            return ResponseEntity.badRequest().body(error);
        }
    }
}
