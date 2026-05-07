package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "makeup_reviews")
public class MakeupReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String customerName; // Đã sửa từ customer_name
    private int rating;          // Chuyển sang int để React dễ xử lý số sao
    private String comment;

    @ManyToOne
    @JoinColumn(name = "makeup_artist_id")
    @JsonIgnore
    private MakeupArtist makeupArtist;

    // --- GETTERS & SETTERS ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public MakeupArtist getMakeupArtist() { return makeupArtist; }
    public void setMakeupArtist(MakeupArtist makeupArtist) { this.makeupArtist = makeupArtist; }
}