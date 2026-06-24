package com.example.demo.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Data
public class BlogPostUpsertRequest {
    private String title;
    private String slug;
    private String category;
    private String categoryLabel;
    private String excerpt;
    private String content;
    private String authorName;
    private String authorTitle;
    private Integer readTimeMinutes;
    private String tags;
    private Boolean published;
    private LocalDateTime publishedAt;
    private MultipartFile coverImageFile;
}
