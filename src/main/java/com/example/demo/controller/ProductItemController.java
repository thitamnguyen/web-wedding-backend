package com.example.demo.controller;

import com.example.demo.dto.ProductDetailResponse;
import com.example.demo.model.ProductItem;
import com.example.demo.repository.ProductItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/product-items")
@CrossOrigin(origins = "*")
public class ProductItemController {

    @Autowired
    private ProductItemRepository productItemRepository;

    // Lấy toàn bộ sản phẩm không phân biệt ẩn hiện dành cho Admin quản lý
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public List<ProductItem> getAllProductsForAdmin() {
        return productItemRepository.findAll();
    }

    @GetMapping
    public List<ProductItem> getPublishedProducts() {
        return productItemRepository.findByPublishedTrueOrderByPublishedAtDesc();
    }

    // --- THÊM MỚI SẢN PHẨM ---
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createProductItem(@RequestBody ProductItem item) {
        try {
            if(item.getSlug() == null || item.getSlug().isEmpty()) {
                item.setSlug("prod-" + System.currentTimeMillis());
            }
            item.setPublishedAt(LocalDateTime.now());
            item.setPublished(true);

            // Tự động map Label nếu FE quên gửi
            if(item.getCategoryKey() != null && item.getCategoryLabel() == null) {
                switch(item.getCategoryKey()) {
                    case "concept-noi-bat": item.setCategoryLabel("Concept Nổi Bật"); break;
                    case "album-pre-wedding": item.setCategoryLabel("Album Pre-Wedding"); break;
                    case "bst-vay-cuoi": item.setCategoryLabel("BST Váy Cưới"); break;
                    case "album-phong-su-cuoi": item.setCategoryLabel("Phóng Sự Cưới"); break;
                    case "bridal-makeup": item.setCategoryLabel("Bridal Makeup"); break;
                }
            }

            ProductItem saved = productItemRepository.save(item);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi thêm sản phẩm: " + e.getMessage());
        }
    }

    // --- CẬP NHẬT SẢN PHẨM ---
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateProductItem(@PathVariable Long id, @RequestBody ProductItem details) {
        return productItemRepository.findById(id).map(item -> {
            item.setTitle(details.getTitle());
            item.setCategoryKey(details.getCategoryKey());
            item.setCategoryLabel(details.getCategoryLabel());
            item.setExcerpt(details.getExcerpt());
            item.setContent(details.getContent());
            item.setCoverImageUrl(details.getCoverImageUrl());
            item.setPriceRange(details.getPriceRange());
            item.setBadge(details.getBadge());
            item.setPhotographerId(details.getPhotographerId());
            item.setMakeupArtistId(details.getMakeupArtistId());

            ProductItem updated = productItemRepository.save(item);
            return ResponseEntity.ok(updated);
        }).orElse(ResponseEntity.notFound().build());
    }

    // --- XÓA SẢN PHẨM ---
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteProductItem(@PathVariable Long id) {
        return productItemRepository.findById(id).map(item -> {
            productItemRepository.delete(item);
            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }
}