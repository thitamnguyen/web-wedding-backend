package com.example.demo.dto;

public record ProfileBookingDto(
        Long id,
        String customerName,
        String bookingDate,
        String serviceTitle,
        String serviceImageUrl,
        String status,
        String paymentStatus,
        Double totalPrice,
        Double depositAmount,
        Long photographerId,
        String photographerName,
        Long makeupArtistId,
        String makeupArtistName,
        String message,
        boolean hasReview,
        String reviewLabel
) {
}
