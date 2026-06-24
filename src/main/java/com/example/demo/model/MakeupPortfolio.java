package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "makeup_portfolio")
public class MakeupPortfolio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String imageUrl;
    @Column(name = "public_id")
    private String publicId;
    private String styleName;

    @ManyToOne
    @JoinColumn(name = "makeup_artist_id")
    @JsonIgnore // Tránh lỗi vòng lặp JSON
    private MakeupArtist makeupArtist;

    // --- GETTERS & SETTERS ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getPublicId() { return publicId; }
    public void setPublicId(String publicId) { this.publicId = publicId; }

    public String getStyleName() { return styleName; }
    public void setStyleName(String styleName) { this.styleName = styleName; }

    public MakeupArtist getMakeupArtist() { return makeupArtist; }
    public void setMakeupArtist(MakeupArtist makeupArtist) { this.makeupArtist = makeupArtist; }
}
