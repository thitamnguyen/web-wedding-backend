package com.example.demo.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class StaffUpsertRequest {
    private String role;
    private String fullName;
    private String jobTitle;
    private MultipartFile avatarFile;
}
