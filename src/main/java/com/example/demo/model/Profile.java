package com.example.demo.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.Data;

@Entity
@Table(name = "profiles")
@Data
public class Profile {
    @Id
    @Column(name = "user_id")
    private Long userId; // Khóa chính đồng thời là khóa ngoại nối sang users

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "job_title")
    private String jobTitle;

    private String style;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "experience_years")
    private Integer experienceYears;

    @Column(name = "rating", precision = 2, scale = 1)
    private BigDecimal rating;

    @Column(name = "review_count")
    private Integer reviewCount;

    @Column(name = "featured_work")
    private String featuredWork;

    @Column(name = "total_revenue", precision = 14, scale = 2)
    private BigDecimal totalRevenue = BigDecimal.ZERO;
}
