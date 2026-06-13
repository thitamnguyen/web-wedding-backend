package com.example.demo.controller;

import com.example.demo.model.Review;
import com.example.demo.service.ReviewService;
import com.example.demo.repository.ReviewRepository; // Import thêm repo vào đây
import com.example.demo.dto.StaffRatingDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "*")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private ReviewRepository reviewRepository; // Tiêm repo vào để dùng luôn cho nhanh

    @PostMapping("/create")
    public ResponseEntity<?> createReview(@RequestBody Review review) {
        try {
            Review savedReview = reviewService.saveReview(review);
            return ResponseEntity.ok(savedReview);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // API 1: Lấy điểm thợ ảnh
    @GetMapping("/rating/photographer/{id}")
    public ResponseEntity<?> getPhotographerRating(@PathVariable Long id) {
        Double avg = reviewRepository.getAverageRatingForPhotographer(id);
        Long count = reviewRepository.countReviewsForPhotographer(id);
        return ResponseEntity.ok(new StaffRatingDto(avg, count));
    }

    // API 2: Lấy điểm thợ makeup
    @GetMapping("/rating/makeup/{id}")
    public ResponseEntity<?> getMakeupArtistRating(@PathVariable Long id) {
        Double avg = reviewRepository.getAverageRatingForMakeupArtist(id);
        Long count = reviewRepository.countReviewsForMakeupArtist(id);
        return ResponseEntity.ok(new StaffRatingDto(avg, count));
    }
}