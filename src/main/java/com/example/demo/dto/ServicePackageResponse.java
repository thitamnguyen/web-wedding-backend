package com.example.demo.dto;

import lombok.Data;

@Data
public class ServicePackageResponse {

    private Integer id;
    private Integer categoryId;
    private String categoryTitle;
    private String name;
    private String price;
    private Double rawPrice;
    private String outfits;
    private String makeup;
    private String duration;
    private String team;
    private String products;

    public Integer getId() {
        return id;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public String getCategoryTitle() {
        return categoryTitle;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public void setCategoryTitle(String categoryTitle) {
        this.categoryTitle = categoryTitle;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public Double getRawPrice() {
        return rawPrice;
    }

    public void setRawPrice(Double rawPrice) {
        this.rawPrice = rawPrice;
    }

    public String getOutfits() {
        return outfits;
    }

    public void setOutfits(String outfits) {
        this.outfits = outfits;
    }

    public String getMakeup() {
        return makeup;
    }

    public void setMakeup(String makeup) {
        this.makeup = makeup;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public String getProducts() {
        return products;
    }

    public void setProducts(String products) {
        this.products = products;
    }
}
