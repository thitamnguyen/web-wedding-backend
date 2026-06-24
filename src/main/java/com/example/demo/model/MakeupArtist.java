package com.example.demo.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "beauty_experts")
public class MakeupArtist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;
    private String jobTitle;
    private String avatarUrl;
    @Column(name = "public_id")
    private String publicId;
    private String award;
    @Column(name = "user_id")
    private Long userId;
    @Column(columnDefinition = "TEXT") // Sửa lỗi Data too long
    private String description;
    private String specialty;

    @Column(name = "total_revenue", precision = 14, scale = 2)
    private BigDecimal totalRevenue = BigDecimal.ZERO;

    @OneToMany(mappedBy = "makeupArtist", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<MakeupPortfolio> portfolios;

    @OneToMany(mappedBy = "makeupArtist", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<MakeupReview> reviews;

    // --- GETTERS & SETTERS ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getPublicId() { return publicId; }
    public void setPublicId(String publicId) { this.publicId = publicId; }

    public String getAward() { return award; }
    public void setAward(String award) { this.award = award; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSpecialty() { return specialty; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }

    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }

    public List<MakeupPortfolio> getPortfolios() { return portfolios; }
    public void setPortfolios(List<MakeupPortfolio> portfolios) { this.portfolios = portfolios; }

    public List<MakeupReview> getReviews() { return reviews; }
    public void setReviews(List<MakeupReview> reviews) { this.reviews = reviews; }
}
