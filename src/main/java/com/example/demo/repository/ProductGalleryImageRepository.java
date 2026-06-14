package com.example.demo.repository;

import com.example.demo.model.ProductGalleryImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductGalleryImageRepository extends JpaRepository<ProductGalleryImage, Long> {
    List<ProductGalleryImage> findByProductItemIdOrderBySortOrderAscIdAsc(Long productItemId);

    boolean existsByProductItemId(Long productItemId);
}
