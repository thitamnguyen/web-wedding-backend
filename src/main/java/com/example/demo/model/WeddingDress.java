package com.example.demo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "wedding_dresses")
public class WeddingDress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String dressName;

    private String dressType;

    private String bodyShape;

    private String style;

    private Double price;

    private String imageUrl;

    // getter setter

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDressName() {
        return dressName;
    }

    public void setDressName(
            String dressName
    ) {
        this.dressName =
                dressName;
    }

    public String getDressType() {
        return dressType;
    }

    public void setDressType(
            String dressType
    ) {
        this.dressType =
                dressType;
    }

    public String getBodyShape() {
        return bodyShape;
    }

    public void setBodyShape(
            String bodyShape
    ) {
        this.bodyShape =
                bodyShape;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(
            String style
    ) {
        this.style = style;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(
            Double price
    ) {
        this.price = price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(
            String imageUrl
    ) {
        this.imageUrl =
                imageUrl;
    }
}
