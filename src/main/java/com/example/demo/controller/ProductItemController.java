package com.example.demo.controller;

import com.example.demo.model.ProductItem;
import com.example.demo.repository.ProductItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product-items")
@CrossOrigin(origins = "*")
public class ProductItemController {

    @Autowired
    private ProductItemRepository productItemRepository;

    @GetMapping
    public List<ProductItem> getPublishedProducts() {
        return productItemRepository.findByPublishedTrueOrderByPublishedAtDesc();
    }

    @GetMapping("/category/{categoryKey}")
    public List<ProductItem> getProductsByCategory(@PathVariable String categoryKey) {
        return productItemRepository.findByCategoryKeyAndPublishedTrueOrderByPublishedAtDesc(categoryKey);
    }

    @GetMapping("/photographer/{photographerId}")
    public List<ProductItem> getProductsByPhotographer(@PathVariable Long photographerId) {
        return productItemRepository.findByPhotographerIdAndPublishedTrueOrderByPublishedAtDesc(photographerId);
    }

    @GetMapping("/makeup-artist/{makeupArtistId}")
    public List<ProductItem> getProductsByMakeupArtist(@PathVariable Long makeupArtistId) {
        return productItemRepository.findByMakeupArtistIdAndPublishedTrueOrderByPublishedAtDesc(makeupArtistId);
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ProductItem> getProductBySlug(@PathVariable String slug) {
        return productItemRepository.findBySlugAndPublishedTrue(slug)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{slug}/related")
    public List<ProductItem> getRelatedProducts(@PathVariable String slug) {
        return productItemRepository.findBySlugAndPublishedTrue(slug)
                .map(product -> {
                    List<ProductItem> related = productItemRepository
                            .findTop3ByCategoryKeyAndPublishedTrueAndSlugNotOrderByPublishedAtDesc(product.getCategoryKey(), slug);
                    if (!related.isEmpty()) {
                        return related;
                    }
                    return productItemRepository.findTop3ByPublishedTrueAndSlugNotOrderByPublishedAtDesc(slug);
                })
                .orElseGet(List::of);
    }
}
