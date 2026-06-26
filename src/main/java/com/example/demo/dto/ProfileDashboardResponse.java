package com.example.demo.dto;

import java.util.List;

public record ProfileDashboardResponse(
        ProfileUserDto user,
        ProfileSummaryDto summary,
        List<ProfileBookingDto> bookings,
        List<ProfileFavoriteConceptDto> favoriteConcepts,
        List<ProfileAiDressFavoriteDto> favoriteAiDresses,
        List<ProfileAlbumDto> albums,
        List<ProfilePaymentDto> payments,
        List<ProfileReviewDto> reviews
) {
}
