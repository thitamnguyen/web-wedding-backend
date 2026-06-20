package com.example.demo.dto;

import java.util.List;

public record StaffDashboardResponse(
        ProfileUserDto user,
        StaffProfileDto profile,
        StaffSummaryDto summary,
        List<StaffScheduleDto> schedule,
        List<StaffWorkDto> works,
        List<StaffRevenuePointDto> revenue
) {
}
