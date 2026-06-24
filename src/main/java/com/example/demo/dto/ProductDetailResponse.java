package com.example.demo.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ProductDetailResponse {
    private Long id;
    private String title;
    private String slug;
    private String categoryKey;
    private String categoryLabel;
    private String excerpt;
    private String content;
    private String coverImageUrl;
    private String publicId;
    private Long photographerId;
    private Long makeupArtistId;
    private String priceRange;
    private String badge;
    private LocalDateTime publishedAt;
    private Boolean published;
    private List<ProductGalleryImageResponse> galleryImages;
    private Double averageRating;
    private Long reviewCount;

    public ProductDetailResponse(
            Long id,
            String title,
            String slug,
            String categoryKey,
            String categoryLabel,
            String excerpt,
            String content,
            String coverImageUrl,
            String publicId,
            Long photographerId,
            Long makeupArtistId,
            String priceRange,
            String badge,
            LocalDateTime publishedAt,
            Boolean published,
            List<ProductGalleryImageResponse> galleryImages
    ) {
        this.id = id;
        this.title = title;
        this.slug = slug;
        this.categoryKey = categoryKey;
        this.categoryLabel = categoryLabel;
        this.excerpt = excerpt;
        this.content = content;
        this.coverImageUrl = coverImageUrl;
        this.publicId = publicId;
        this.photographerId = photographerId;
        this.makeupArtistId = makeupArtistId;
        this.priceRange = priceRange;
        this.badge = badge;
        this.publishedAt = publishedAt;
        this.published = published;
        this.galleryImages = galleryImages;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getSlug() {
        return slug;
    }

    public String getCategoryKey() {
        return categoryKey;
    }

    public String getCategoryLabel() {
        return categoryLabel;
    }

    public String getExcerpt() {
        return excerpt;
    }

    public String getContent() {
        return content;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public String getPublicId() {
        return publicId;
    }

    public Long getPhotographerId() {
        return photographerId;
    }

    public Long getMakeupArtistId() {
        return makeupArtistId;
    }

    public String getPriceRange() {
        return priceRange;
    }

    public String getBadge() {
        return badge;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public Boolean getPublished() {
        return published;
    }

    public List<ProductGalleryImageResponse> getGalleryImages() {
        return galleryImages;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public Long getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(Long reviewCount) {
        this.reviewCount = reviewCount;
    }
}
