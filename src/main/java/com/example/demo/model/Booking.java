package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Data
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(name = "customer_phone", nullable = false)
    private String customerPhone;

    @Column(name = "customer_email", nullable = false)
    private String customerEmail;

    @Column(name = "booking_date", nullable = false)
    private LocalDate bookingDate;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "total_price")
    private Double totalPrice = 0.0;

    @Column(name = "payment_status")
    private String paymentStatus = "UNPAID";

    private String status = "PENDING";

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    // =========================================================================
    // CẤU HÌNH QUAN HỆ ĐỐI TƯỢNG (JOIN BẢNG) ĐỂ LẤY TÊN HIỂN THỊ
    // =========================================================================

    @Column(name = "service_id")
    private Long serviceId;

    @ManyToOne
    @JoinColumn(name = "service_id", insertable = false, updatable = false)
    private WeddingService weddingService; // Lấy thông tin gói dịch vụ cưới

    @Column(name = "photographer_id")
    private Long photographerId;

    @ManyToOne
    @JoinColumn(name = "photographer_id", insertable = false, updatable = false)
    private Profile photographerProfile; // Lấy thông tin nhiếp ảnh gia từ bảng profiles

    @Column(name = "makeup_artist_id")
    private Long makeupArtistId;

    @ManyToOne
    @JoinColumn(name = "makeup_artist_id", insertable = false, updatable = false)
    private MakeupArtist makeupArtist; // Lấy thông tin makeup artist từ bảng beauty_experts
}