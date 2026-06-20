package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "promotions")
@Data
public class Promotion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;            // Ví dụ: "Ưu đãi mùa cưới Tháng 6"
    private String description;     // Ví dụ: "Giảm giá trực tiếp cho toàn bộ các gói combo"
    private Double discountPercentage;
    private LocalDate startDate;
    private LocalDate endDate;
    private String promotionType;   // "ALL" (tất cả), "PACKAGE" (chỉ một số gói)
    private Boolean active;
}