package com.example.demo.dto;

import java.time.LocalDateTime;

public record StaffWorkDto(
        Long id,
        String title,
        String slug,
        String categoryLabel,
        String excerpt,
        String coverImageUrl,
        String priceRange,
        String badge,
        LocalDateTime publishedAt,
        Double averageRating,
        Long reviewCount
) {
}
