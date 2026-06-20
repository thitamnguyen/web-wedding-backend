package com.example.demo.repository;

import com.example.demo.model.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {
    // Tìm các khuyến mãi đang trong thời gian hiệu lực và đang bật (active)
    @Query("SELECT p FROM Promotion p WHERE p.active = true AND :date BETWEEN p.startDate AND p.endDate")
    List<Promotion> findActivePromotionsByDate(@Param("date") LocalDate date);
}