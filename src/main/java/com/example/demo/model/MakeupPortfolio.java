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

    public String getStyleName() { return styleName; }
    public void setStyleName(String styleName) { this.styleName = styleName; }

    public MakeupArtist getMakeupArtist() { return makeupArtist; }
    public void setMakeupArtist(MakeupArtist makeupArtist) { this.makeupArtist = makeupArtist; }
}