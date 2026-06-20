package com.example.demo.dto;

public record StaffScheduleDto(
        Long bookingId,
        String customerName,
        String bookingDate,
        String serviceTitle,
        String status,
        String paymentStatus,
        Double totalPrice,
        Double deposit,
        String location,
        String message,
        Long userId
) {
}
