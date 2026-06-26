package com.example.demo.dto;

import lombok.Data;

@Data
public class ServicePackageUpsertRequest {
    private String name;
    private Double price;
    private String outfits;
    private String makeup;
    private String duration;
    private String team;
    private String products;
    private Integer categoryId;
}
