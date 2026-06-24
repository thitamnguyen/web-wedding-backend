package com.example.demo.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ProductItemUpsertRequest {
    private String title;
    private String categoryKey;
    private String categoryLabel;
    private String excerpt;
    private String content;
    private String priceRange;
    private String badge;
    private String slug;
    private Long photographerId;
    private Long makeupArtistId;
    private Boolean published;
    private MultipartFile coverImageFile;
    private MultipartFile[] galleryFiles;
}
