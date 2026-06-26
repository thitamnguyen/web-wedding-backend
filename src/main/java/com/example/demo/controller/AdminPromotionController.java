package com.example.demo.controller;

import com.example.demo.model.Promotion;
import com.example.demo.repository.PromotionRepository;
import com.example.demo.service.PromotionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class AdminPromotionController {

    private final PromotionRepository promotionRepository;
    private final PromotionService promotionService;

    public AdminPromotionController(PromotionRepository promotionRepository, PromotionService promotionService) {
        this.promotionRepository = promotionRepository;
        this.promotionService = promotionService;
    }

    @GetMapping("/admin/promotions")
    public List<Promotion> getAllPromotions() {
        return promotionService.getAllPromotionsNewestFirst();
    }

    @PostMapping("/admin/promotions")
    public Promotion createPromotion(@RequestBody Promotion promotion) {
        if (promotion.getActive() == null) {
            promotion.setActive(true);
        }
        if (promotion.getCode() == null || promotion.getCode().isBlank()) {
            promotion.setCode(generateDefaultCode(promotion));
        }
        return promotionRepository.save(promotion);
    }

    @PutMapping("/admin/promotions/{id}")
    public Promotion updatePromotion(@PathVariable Long id, @RequestBody Promotion promotion) {
        Promotion current = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ưu đãi"));

        current.setCode((promotion.getCode() == null || promotion.getCode().isBlank())
                ? current.getCode()
                : promotion.getCode().trim());
        current.setName(promotion.getName());
        current.setDescription(promotion.getDescription());
        current.setDiscountPercentage(promotion.getDiscountPercentage());
        current.setStartDate(promotion.getStartDate());
        current.setEndDate(promotion.getEndDate());
        current.setPromotionType(promotion.getPromotionType());
        current.setActive(promotion.getActive() != null ? promotion.getActive() : current.getActive());

        return promotionRepository.save(current);
    }

    @DeleteMapping("/admin/promotions/{id}")
    public ResponseEntity<String> deletePromotion(@PathVariable Long id) {
        promotionRepository.deleteById(id);
        return ResponseEntity.ok("Đã xóa ưu đãi thành công");
    }

    @GetMapping("/promotions/active")
    public List<Promotion> getActivePromotions() {
        return promotionService.getActivePromotions();
    }

    @GetMapping("/promotions/available-for-booking")
    public List<Promotion> getAvailablePromotionsForBooking(@RequestParam(required = false) Long userId) {
        return promotionService.getActivePromotionsForUser(userId);
    }

    @GetMapping("/current-active")
    public List<Promotion> getCurrentActivePromotions() {
        return promotionService.getActivePromotions();
    }

    @GetMapping("/promotions/current-active")
    public List<Promotion> getCurrentActivePromotionsAlias() {
        return promotionService.getActivePromotions();
    }

    @GetMapping("/promotions/validate")
    public ResponseEntity<?> validatePromotion(@RequestParam String code, @RequestParam(required = false) Long userId) {
        return promotionService.findPromotionByCode(code)
                .map(promo -> {
                    if (promotionService.isPromotionUsedByUser(userId, promo)) {
                        return ResponseEntity.ok(Map.of(
                                "valid", false,
                                "message", "Ưu đãi này đã được sử dụng rồi."
                        ));
                    }

                    return ResponseEntity.ok(Map.of(
                            "valid", true,
                            "promotion", promo
                    ));
                })
                .orElseGet(() -> ResponseEntity.ok(Map.of(
                        "valid", false,
                        "message", "Mã ưu đãi không hợp lệ hoặc đã hết hạn"
                )));
    }

    @DeleteMapping("/promotions/cleanup-expired")
    public ResponseEntity<?> cleanupExpiredPromotions() {
        promotionService.cleanupExpiredPromotions();
        return ResponseEntity.ok(Map.of("message", "Đã dọn ưu đãi hết hạn"));
    }

    @PostMapping("/admin/promotions/cleanup-expired")
    public ResponseEntity<?> triggerManualCleanup() {
        String result = promotionService.manualCleanupExpiredPromotions();
        return ResponseEntity.ok(Map.of("message", result));
    }

    private String generateDefaultCode(Promotion promotion) {
        String source = promotion.getName() != null ? promotion.getName() : "PROMO";
        String normalized = source.trim().toUpperCase()
                .replaceAll("[^A-Z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
        return normalized.isBlank() ? "PROMO" : normalized;
    }
}
