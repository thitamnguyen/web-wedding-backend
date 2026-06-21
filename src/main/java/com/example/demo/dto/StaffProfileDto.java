package com.example.demo.dto;

import java.math.BigDecimal;

public record StaffProfileDto(
        Long id,
        String fullName,
        String jobTitle,
        String avatarUrl,
        String style,
        String specialty,
        String description,
        Integer experienceYears,
        Double rating,
        Integer reviewCount,
        String featuredWork,
        BigDecimal totalRevenue,
        String staffType
) {
}
