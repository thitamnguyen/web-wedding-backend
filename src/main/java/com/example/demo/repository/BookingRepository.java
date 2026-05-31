package com.example.demo.repository;

import com.example.demo.model.Booking;
import org.hibernate.query.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    // Tự động đếm số lượng đơn theo trạng thái (Không cần viết SQL)
    long countByStatus(String status);

    // Tìm tất cả lịch đặt theo trạng thái (Ví dụ: Tìm tất cả lịch đang PENDING)
    List<Booking> findByStatus(String status);

    // 🌟 THÊM 2 DÒNG NÀY VÀO ĐỂ FIX TRIỆT ĐỂ LỖI ĐỎ Ở SERVICE
    boolean existsByPhotographerIdAndBookingDateAndStatus(Long photographerId, LocalDate bookingDate, String status);

    boolean existsByMakeupArtistIdAndBookingDateAndStatus(Long makeupArtistId, LocalDate bookingDate, String status);
    //ham tiem kiem ds bôking theo sdt
    java.util.List<Booking> findByCustomerPhoneOrderByBookingDateDesc(String customerPhone);
    java.util.List<Booking> findByPhotographerIdAndStatus(Long photographerId, String status);
    java.util.List<Booking> findByMakeupArtistIdAndStatus(Long makeupArtistId, String status);
}