package com.example.demo.dto;

public record ProfileAiDressFavoriteDto(
        Long id,
        Long dressId,
        String dressName,
        String dressType,
        String style,
        String bodyShape,
        Double price,
        String imageUrl,
        String description,
        boolean liked
) {
}
