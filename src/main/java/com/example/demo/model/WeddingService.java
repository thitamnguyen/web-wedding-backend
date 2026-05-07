package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "wedding_services")
@Data // Nếu không dùng Lombok thì em tự generate Getter/Setter nhé
public class WeddingService {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String shortDescription;
    private String imageUrl;
    private String iconName;

    @Column(columnDefinition = "LONGTEXT")
    private String detailContent;
}
