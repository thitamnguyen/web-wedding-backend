package com.example.demo.repository;

import com.example.demo.model.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Tự động đếm số lượng đơn theo trạng thái
    long countByStatus(String status);

    // Tìm kiếm tất cả đơn hàng (có phân trang chuẩn Spring Data)
    Page<Booking> findAll(Pageable pageable);

    // Tìm kiếm đơn hàng theo trạng thái lọc (có phân trang chuẩn Spring Data)
    Page<Booking> findByStatus(String status, Pageable pageable);

    // Tìm tất cả lịch đặt theo trạng thái không phân trang (Dùng để tính doanh thu ở Dashboard)
    List<Booking> findByStatus(String status);

    // Kiểm tra trùng lịch cho Nhiếp ảnh gia và Thợ Makeup
    boolean existsByPhotographerIdAndBookingDateAndStatus(Long photographerId, LocalDate bookingDate, String status);

    boolean existsByMakeupArtistIdAndBookingDateAndStatus(Long makeupArtistId, LocalDate bookingDate, String status);

    // Tìm kiếm danh sách đặt lịch theo số điện thoại
    List<Booking> findByCustomerPhoneOrderByBookingDateDesc(String customerPhone);

    List<Booking> findByPhotographerIdAndStatus(Long photographerId, String status);

    List<Booking> findByMakeupArtistIdAndStatus(Long makeupArtistId, String status);

    // Lấy danh sách các ngày bận của Nhiếp ảnh gia
    @Query("SELECT b.bookingDate FROM Booking b WHERE b.photographerId = :photographerId AND (b.status = 'CONFIRMED' OR b.status = 'DONE')")
    List<LocalDate> findBusyDatesForPhotographer(Long photographerId);

    // Lấy danh sách các ngày bận của Thợ Makeup
    @Query("SELECT b.bookingDate FROM Booking b WHERE b.makeupArtistId = :makeupArtistId AND (b.status = 'CONFIRMED' OR b.status = 'DONE')")
    List<LocalDate> findBusyDatesForMakeupArtist(Long makeupArtistId);
}