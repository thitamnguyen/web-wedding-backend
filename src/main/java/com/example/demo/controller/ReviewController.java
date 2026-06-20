package com.example.demo.controller;

import com.example.demo.dto.StaffRatingDto;
import com.example.demo.model.Review;
import com.example.demo.repository.ReviewRepository;
import com.example.demo.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "*")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private ReviewRepository reviewRepository;

    @PostMapping("/create")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<?> createReview(@RequestBody Review review) {
        try {
            Review savedReview = reviewService.saveReview(review);
            return ResponseEntity.ok(savedReview);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/rating/photographer/{id}")
    public ResponseEntity<?> getPhotographerRating(@PathVariable Long id) {
        Double avg = reviewRepository.getAverageRatingForPhotographer(id);
        Long count = reviewRepository.countReviewsForPhotographer(id);
        return ResponseEntity.ok(new StaffRatingDto(avg, count));
    }

    @GetMapping("/rating/makeup/{id}")
    public ResponseEntity<?> getMakeupArtistRating(@PathVariable Long id) {
        Double avg = reviewRepository.getAverageRatingForMakeupArtist(id);
        Long count = reviewRepository.countReviewsForMakeupArtist(id);
        return ResponseEntity.ok(new StaffRatingDto(avg, count));
    }
}
