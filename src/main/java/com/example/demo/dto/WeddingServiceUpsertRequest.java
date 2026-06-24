package com.example.demo.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class WeddingServiceUpsertRequest {
    private String title;
    private String shortDescription;
    private String priceRange;
    private String iconName;
    private String detailedDescription;
    private MultipartFile imageFile;
}
