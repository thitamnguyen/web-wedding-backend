package com.example.demo.dto;

public class ProductGalleryImageResponse {
    private Long id;
    private String imageUrl;
    private String publicId;
    private Integer sortOrder;

    public ProductGalleryImageResponse(Long id, String imageUrl, String publicId, Integer sortOrder) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.publicId = publicId;
        this.sortOrder = sortOrder;
    }

    public Long getId() {
        return id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getPublicId() {
        return publicId;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }
}
