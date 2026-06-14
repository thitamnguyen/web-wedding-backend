package com.example.demo.controller;

import com.example.demo.dto.ProductDetailResponse;
import com.example.demo.dto.ProductGalleryImageResponse;
import com.example.demo.model.ProductGalleryImage;
import com.example.demo.model.ProductItem;
import com.example.demo.repository.ProductGalleryImageRepository;
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

    @Autowired
    private ProductGalleryImageRepository productGalleryImageRepository;

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
    public ResponseEntity<ProductDetailResponse> getProductBySlug(@PathVariable String slug) {
        return productItemRepository.findBySlugAndPublishedTrue(slug)
                .map(this::toDetailResponse)
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

    private ProductDetailResponse toDetailResponse(ProductItem product) {
        List<ProductGalleryImageResponse> galleryImages = productGalleryImageRepository
                .findByProductItemIdOrderBySortOrderAscIdAsc(product.getId())
                .stream()
                .map(this::toGalleryResponse)
                .toList();

        return new ProductDetailResponse(
                product.getId(),
                product.getTitle(),
                product.getSlug(),
                product.getCategoryKey(),
                product.getCategoryLabel(),
                product.getExcerpt(),
                product.getContent(),
                product.getCoverImageUrl(),
                product.getPhotographerId(),
                product.getMakeupArtistId(),
                product.getPriceRange(),
                product.getBadge(),
                product.getPublishedAt(),
                product.getPublished(),
                galleryImages
        );
    }

    private ProductGalleryImageResponse toGalleryResponse(ProductGalleryImage image) {
        return new ProductGalleryImageResponse(
                image.getId(),
                image.getImageUrl(),
                image.getSortOrder()
        );
    }
}
