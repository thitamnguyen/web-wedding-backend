package com.example.demo.dto;

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
        String staffType
) {
}
