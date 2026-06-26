package com.example.demo.repository;

import com.example.demo.model.AiDressFavorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AiDressFavoriteRepository extends JpaRepository<AiDressFavorite, Long> {
    List<AiDressFavorite> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<AiDressFavorite> findByUserIdAndDressId(Long userId, Long dressId);

    boolean existsByUserIdAndDressId(Long userId, Long dressId);
}
