package com.example.demo.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ServiceCategoryUpsertRequest {
    private String categoryCode;
    private String title;
    private String tagline;
    private String subTitle;
    private String description;
    private MultipartFile imageFile;
}
