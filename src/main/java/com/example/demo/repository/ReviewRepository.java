package com.example.demo.repository;

import com.example.demo.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByBookingId(Long bookingId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.photographerId = :photographerId")
    Double getAverageRatingForPhotographer(Long photographerId);

    // Đếm tổng số đánh giá của thợ ảnh
    @Query("SELECT COUNT(r) FROM Review r WHERE r.photographerId = :photographerId")
    Long countReviewsForPhotographer(Long photographerId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.makeupArtistId = :makeupArtistId")
    Double getAverageRatingForMakeupArtist(Long makeupArtistId);

    // Đếm tổng số đánh giá của thợ makeup
    @Query("SELECT COUNT(r) FROM Review r WHERE r.makeupArtistId = :makeupArtistId")
    Long countReviewsForMakeupArtist(Long makeupArtistId);
}