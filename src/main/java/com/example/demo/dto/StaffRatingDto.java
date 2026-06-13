package com.example.demo.dto;

public class StaffRatingDto {
    private Double averageRating;
    private Long totalReviews;

    public StaffRatingDto(Double averageRating, Long totalReviews) {
        this.averageRating = averageRating != null ? Math.round(averageRating * 10) / 10.0 : 0.0; // Làm tròn 1 chữ số thập phân
        this.totalReviews = totalReviews != null ? totalReviews : 0L;
    }

    // --- Getter và Setter ---
    public Double getAverageRating() { return averageRating; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }
    public Long getTotalReviews() { return totalReviews; }
    public void setTotalReviews(Long totalReviews) { this.totalReviews = totalReviews; }
}