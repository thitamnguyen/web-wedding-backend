package com.example.demo.repository;

import com.example.demo.model.ConceptFavorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConceptFavoriteRepository extends JpaRepository<ConceptFavorite, Long> {
    List<ConceptFavorite> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<ConceptFavorite> findByUserIdAndProductItemId(Long userId, Long productItemId);

    boolean existsByUserIdAndProductItemId(Long userId, Long productItemId);

    long countByProductItemId(Long productItemId);
}
