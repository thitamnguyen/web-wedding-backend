package com.example.demo.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class StaffWorkCreateRequest {
    private String title;
    private String excerpt;
    private String content;
    private String badge;
    // Kept for backward compatibility, but backend now derives price from booking.totalPrice.
    private String priceRange;
    private String slug;
    private String categoryKey;
    private String categoryLabel;
    private Long bookingId;
    private MultipartFile coverImageFile;
    private MultipartFile[] galleryFiles;
}
