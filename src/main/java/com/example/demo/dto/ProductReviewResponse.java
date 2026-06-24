package com.example.demo.dto;

import java.time.LocalDateTime;

public record ProductReviewResponse(
        Long id,
        String customerName,
        Integer rating,
        String comment,
        LocalDateTime createdAt
) {
}
