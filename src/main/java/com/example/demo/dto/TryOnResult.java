package com.example.demo.dto;

public class TryOnResult {
    private final byte[] imageBytes;
    private final String mode;
    private final String notice;

    public TryOnResult(byte[] imageBytes, String mode, String notice) {
        this.imageBytes = imageBytes;
        this.mode = mode;
        this.notice = notice;
    }

    public byte[] getImageBytes() {
        return imageBytes;
    }

    public String getMode() {
        return mode;
    }

    public String getNotice() {
        return notice;
    }
}
