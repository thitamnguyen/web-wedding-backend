package com.example.demo.dto;

import lombok.Data;
import java.util.List;

@Data
public class ServiceCategoryResponse {

    private Integer dbId;
    private String id;
    private String title;
    private String tagline;
    private String image;
    private String subTitle;
    private String desc;
    private String publicId;

    private List<ServicePackageResponse> packages;

    public Integer getDbId() {
        return dbId;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getTagline() {
        return tagline;
    }

    public String getImage() {
        return image;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public String getDesc() {
        return desc;
    }

    public String getPublicId() {
        return publicId;
    }

    public List<ServicePackageResponse> getPackages() {
        return packages;
    }

    public void setDbId(Integer dbId) {
        this.dbId = dbId;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setTagline(String tagline) {
        this.tagline = tagline;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }

    public void setPackages(List<ServicePackageResponse> packages) {
        this.packages = packages;
    }
}
