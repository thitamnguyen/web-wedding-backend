package com.example.demo.dto;

public record ProfileAlbumDto(
        Long bookingId,
        String slug,
        String title,
        String bookingDate,
        String coverImageUrl,
        String subtitle,
        String status,
        String photographerName,
        String makeupArtistName,
        String conceptNote,
        Double averageRating,
        Long reviewCount
) {
}
