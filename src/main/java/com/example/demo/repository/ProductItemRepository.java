package com.example.demo.repository;

import com.example.demo.model.ProductItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductItemRepository extends JpaRepository<ProductItem, Long> {
    List<ProductItem> findByPublishedTrueOrderByPublishedAtDesc();

    List<ProductItem> findByCategoryKeyAndPublishedTrueOrderByPublishedAtDesc(String categoryKey);

    Optional<ProductItem> findBySlugAndPublishedTrue(String slug);

    List<ProductItem> findTop3ByCategoryKeyAndPublishedTrueAndSlugNotOrderByPublishedAtDesc(String categoryKey, String slug);

    List<ProductItem> findTop3ByPublishedTrueAndSlugNotOrderByPublishedAtDesc(String slug);

    List<ProductItem> findByPhotographerIdAndPublishedTrueOrderByPublishedAtDesc(Long photographerId);

    List<ProductItem> findByMakeupArtistIdAndPublishedTrueOrderByPublishedAtDesc(Long makeupArtistId);

    List<ProductItem> findByBookingIdIn(List<Long> bookingIds);

    java.util.Optional<ProductItem> findByBookingId(Long bookingId);
}
