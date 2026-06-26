package com.example.demo.repository;

import com.example.demo.model.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {
    List<Promotion> findByActiveTrueOrderByStartDateDescIdDesc();

    // Tìm các khuyến mãi đang trong thời gian hiệu lực và đang bật (active)
    @Query("SELECT p FROM Promotion p WHERE p.active = true AND :date BETWEEN p.startDate AND p.endDate")
    List<Promotion> findActivePromotionsByDate(@Param("date") LocalDate date);
    List<Promotion> findByActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByStartDateDescIdDesc(
            LocalDate startDate,
            LocalDate endDate
    );

    Optional<Promotion> findByCodeIgnoreCaseAndActiveTrue(String code);

    Optional<Promotion> findByCodeIgnoreCase(String code);

    Optional<Promotion> findByNameIgnoreCase(String name);

    @Modifying
    @Query("delete from Promotion p where p.endDate < :today")
    void deleteExpiredPromotions(@Param("today") LocalDate today);
}
