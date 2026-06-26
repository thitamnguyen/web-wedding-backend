package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title; // Ví dụ: "Đơn đặt lịch mới!"

    @Column(columnDefinition = "TEXT", nullable = false)
    private String message; // Ví dụ: "Khách hàng Nguyễn Văn A vừa đặt gói Combo..."

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false; // false = Chưa đọc, true = Đã đọc

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "booking_id")
    private Long bookingId; // Lưu ID đơn đặt lịch để khi bấm vào có thể nhảy tới đơn đó
}