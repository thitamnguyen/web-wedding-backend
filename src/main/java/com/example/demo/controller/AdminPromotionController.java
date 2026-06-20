package com.example.demo.controller;

import com.example.demo.model.Promotion;
import com.example.demo.repository.PromotionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@CrossOrigin(origins = "*")
public class AdminPromotionController {

    @Autowired
    private PromotionRepository promotionRepository;

    // Endpoint dành cho Admin quản lý ưu đãi
    @GetMapping("/api/admin/promotions")
    public List<Promotion> getAllPromotions() {
        return promotionRepository.findAll();
    }

    @PostMapping("/api/admin/promotions")
    public Promotion createPromotion(@RequestBody Promotion promotion) {
        if (promotion.getActive() == null) {
            promotion.setActive(true);
        }
        return promotionRepository.save(promotion);
    }

    @DeleteMapping("/api/admin/promotions/{id}")
    public ResponseEntity<String> deletePromotion(@PathVariable Long id) {
        promotionRepository.deleteById(id);
        return ResponseEntity.ok("Đã xóa ưu đãi thành công");
    }

    // BỔ SUNG: Endpoint công khai cho Trang chủ và Trang dịch vụ của Khách hàng
    // Khớp 100% với endpoint axios.get('http://localhost:8080/api/current-active') mà em viết ở Home.jsx
    @GetMapping("/api/current-active")
    public List<Promotion> getCurrentActivePromotions() {
        return promotionRepository.findActivePromotionsByDate(LocalDate.now());
    }
}