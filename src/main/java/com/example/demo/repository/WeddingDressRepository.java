package com.example.demo.repository;

import com.example.demo.model.WeddingDress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface WeddingDressRepository
        extends JpaRepository<
        WeddingDress,
        Long> {

    List<WeddingDress>
    findByBodyShape(
            String bodyShape
    );

    @Query("""
        SELECT w
        FROM WeddingDress w
        WHERE w.bodyShape = :bodyShape
        AND (:style IS NULL
            OR w.style = :style)
        AND (:budget IS NULL
            OR w.price <= :budget)
    """)
    List<WeddingDress>
    findRecommendedDress(
            String bodyShape,
            String style,
            Long budget
    );
}