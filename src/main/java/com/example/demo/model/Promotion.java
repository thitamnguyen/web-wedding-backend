package com.example.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "promotions")
@Data
public class Promotion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String code;

    private String name;
    private String description;
    private Double discountPercentage;
    private LocalDate startDate;
    private LocalDate endDate;
    private String promotionType;   // ALL / PACKAGE
    private Boolean active;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Transient
    private Boolean usedByUser = false;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
