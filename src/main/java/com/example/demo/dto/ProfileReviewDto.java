package com.example.demo.dto;

public record ProfileReviewDto(
        Long reviewId,
        Long bookingId,
        String serviceTitle,
        Integer rating,
        String comment,
        String createdAt,
        String photographerName,
        String makeupArtistName,
        String bookingDate,
        boolean reviewable
) {
}
