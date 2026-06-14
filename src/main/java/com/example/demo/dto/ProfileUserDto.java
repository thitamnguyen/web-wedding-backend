package com.example.demo.dto;

public record ProfileUserDto(
        Long id,
        String fullName,
        String email,
        String phone,
        String role,
        String initials
) {
}
