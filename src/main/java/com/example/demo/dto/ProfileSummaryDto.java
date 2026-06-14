package com.example.demo.dto;

public record ProfileSummaryDto(
        long totalBookings,
        long upcomingBookings,
        long completedBookings,
        long favoriteConcepts,
        long reviews,
        long unpaidBookings,
        double totalSpent
) {
}
