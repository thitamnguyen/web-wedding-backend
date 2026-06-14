package com.example.demo.dto;

public record ProfilePaymentDto(
        Long bookingId,
        String serviceTitle,
        String bookingDate,
        Double totalPrice,
        Double depositAmount,
        Double remainingAmount,
        String paymentStatus,
        String status,
        String bankContent,
        String qrImageUrl
) {
}
