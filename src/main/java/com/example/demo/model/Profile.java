package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "profiles")
@Data
public class Profile {
    @Id
    @Column(name = "user_id")
    private Long userId; // Khóa chính đồng thời là khóa ngoại nối sang users

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "job_title")
    private String jobTitle;

    private String style;

    @Column(name = "avatar_url")
    private String avatarUrl;
}