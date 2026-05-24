package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "wedding_services")
@Data
public class WeddingService {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(name = "short_description")
    private String shortDescription;

    @Column(name = "price_range")
    private String priceRange;

    @Column(name = "icon_name")
    private String iconName;

    private String imageUrl;
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    @Column(name = "detailed_description", columnDefinition = "TEXT")
    private String detailedDescription;
}