package com.example.demo.dto;

public record StaffSummaryDto(
        long totalBookings,
        long upcomingBookings,
        long completedBookings,
        long pendingBookings,
        long worksCount,
        double totalRevenue,
        double monthRevenue
) {
}
