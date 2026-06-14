package com.example.demo.dto;

public class ProductGalleryImageResponse {
    private Long id;
    private String imageUrl;
    private Integer sortOrder;

    public ProductGalleryImageResponse(Long id, String imageUrl, Integer sortOrder) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.sortOrder = sortOrder;
    }

    public Long getId() {
        return id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }
}
