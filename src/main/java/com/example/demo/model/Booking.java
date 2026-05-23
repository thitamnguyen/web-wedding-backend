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
    private String customerPhone; // <-- ĐẢM BẢO ĐÃ THÊM CỘT NÀY

    @Column(name = "customer_email", nullable = false)
    private String customerEmail;

    @Column(name = "service_id", nullable = false)
    private Long serviceId;

    @Column(name = "photographer_id")
    private Long photographerId;

    @Column(name = "makeup_artist_id")
    private Long makeupArtistId;

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
}