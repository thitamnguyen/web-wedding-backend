package com.example.demo.dto;

public class ImageUploadResult {
    private String secureUrl;
    private String publicId;

    public ImageUploadResult(String secureUrl, String publicId) {
        this.secureUrl = secureUrl;
        this.publicId = publicId;
    }

    public String getSecureUrl() {
        return secureUrl;
    }

    public void setSecureUrl(String secureUrl) {
        this.secureUrl = secureUrl;
    }

    public String getPublicId() {
        return publicId;
    }

    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }
}
