package com.example.demo.repository;

import com.example.demo.model.ProductReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductReviewRepository extends JpaRepository<ProductReview, Long> {
    List<ProductReview> findByProductItemIdOrderByCreatedAtDesc(Long productItemId);

    long countByProductItemId(Long productItemId);

    default Double averageRating(Long productItemId) {
        List<ProductReview> reviews = findByProductItemIdOrderByCreatedAtDesc(productItemId);
        if (reviews.isEmpty()) {
            return 0.0;
        }
        return reviews.stream().mapToInt(ProductReview::getRating).average().orElse(0.0);
    }
}
