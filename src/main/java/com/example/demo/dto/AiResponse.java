package com.example.demo.dto;

public class AiResponse {

    private boolean success;
    private String body_shape;
    private double confidence;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getBody_shape() {
        return body_shape;
    }

    public void setBody_shape(String body_shape) {
        this.body_shape = body_shape;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }
}
