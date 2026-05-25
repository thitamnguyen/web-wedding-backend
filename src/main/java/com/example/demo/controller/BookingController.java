package com.example.demo.controller;

import com.example.demo.model.Booking;
import com.example.demo.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
// Cấu hình linh hoạt: Hỗ trợ cả cổng mặc định của React (3000) và cổng mặc định của Vite (5173) mà nhóm đang dùng
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class BookingController {

    @Autowired
    private BookingService bookingService;

    // =========================================================================
    // 1. NHÓM API QUẢN LÝ ĐƠN ĐẶT LỊCH (BOOKING CORE)
    // =========================================================================

    /**
     * API cho khách hàng gửi form đặt lịch từ React xuống
     */
    @PostMapping("/create")
    public ResponseEntity<?> createBooking(@RequestBody Booking booking) {
        try {
            Booking savedBooking = bookingService.createBooking(booking);
            return ResponseEntity.ok(savedBooking);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi khi đặt lịch: " + e.getMessage());
        }
    }

    /**
     * API cho trang quản trị Admin lấy toàn bộ danh sách đơn đặt lịch
     */
    @GetMapping("/all")
    public ResponseEntity<?> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    /**
     * API cho Admin xử lý Duyệt hoặc Hủy đơn hàng dựa trên ID
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateBookingStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        try {
            Booking updatedBooking = bookingService.updateStatus(id, status);
            return ResponseEntity.ok(updatedBooking);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi cập nhật trạng thái: " + e.getMessage());
        }
    }

    // =========================================================================
    // 2. NHÓM API BỔ SUNG ĐỂ ĐỔ DỮ LIỆU ĐỘNG RA FORM (DÀNH CHO KHÁCH HÀNG CHỌN)
    // =========================================================================

    /**
     * API Mock/Thực tế: Lấy danh sách Gói Dịch Vụ Wedding
     * (Sau này nếu có WeddingService phục vụ riêng, em có thể inject vào, hiện tại viết tạm ở đây để React test form không bị lỗi trống dữ liệu)
     */
    @GetMapping("/services")
    public ResponseEntity<?> getWeddingServices() {
        // Giả lập dữ liệu trả về giống cấu trúc bảng wedding_services của em
        // Khi nào nhóm làm tới file Service của bảng này thì thay bằng: return ResponseEntity.ok(weddingService.getAll());
        return ResponseEntity.ok(bookingService.getMockWeddingServices());
    }

    /**
     * API Mock/Thực tế: Lấy danh sách Nhiếp Ảnh Gia (Photographer)
     */
    @GetMapping("/photographers")
    public ResponseEntity<?> getPhotographers() {
        // Trả về danh sách Artist/Photographer từ database (Khớp với dữ liệu Alex Nguyen, Hoàng Thùy...)
        return ResponseEntity.ok(bookingService.getMockPhotographers());
    }

    /**
     * API Mock/Thực tế: Lấy danh sách Chuyên Gia Trang Điểm (Beauty Experts)
     */
    @GetMapping("/makeup-artists")
    public ResponseEntity<?> getMakeupArtists() {
        // Trả về danh sách Chuyên gia makeup (Khớp với dữ liệu Elena Tran, Lisa Pham...)
        return ResponseEntity.ok(bookingService.getMockMakeupArtists());
    }
}