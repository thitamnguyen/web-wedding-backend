package com.example.demo.dto;

public record ProfileAlbumDto(
        Long bookingId,
        String title,
        String bookingDate,
        String coverImageUrl,
        String subtitle,
        String status,
        String photographerName,
        String makeupArtistName,
        String conceptNote
) {
}
