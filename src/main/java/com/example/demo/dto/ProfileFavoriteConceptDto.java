package com.example.demo.dto;

public record ProfileFavoriteConceptDto(
        Long id,
        Long productItemId,
        String title,
        String slug,
        String excerpt,
        String coverImageUrl,
        String categoryLabel,
        String priceRange,
        String badge,
        boolean liked,
        long favoriteCount
) {
}
