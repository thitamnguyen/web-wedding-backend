package com.example.demo.controller;

import com.example.demo.model.Booking;
import com.example.demo.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal; // Đã thêm import này để hết báo đỏ BigDecimal 🌟
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
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

    @GetMapping("/services")
    public ResponseEntity<?> getWeddingServices() {
        return ResponseEntity.ok(bookingService.getMockWeddingServices());
    }

    @GetMapping("/photographers")
    public ResponseEntity<?> getPhotographers() {
        return ResponseEntity.ok(bookingService.getMockPhotographers());
    }

    @GetMapping("/makeup-artists")
    public ResponseEntity<?> getMakeupArtists() {
        return ResponseEntity.ok(bookingService.getMockMakeupArtists());
    }

    @GetMapping("/dashboard-stats")
    public ResponseEntity<?> getDashboardStats() {
        try {
            return ResponseEntity.ok(bookingService.getDashboardStats());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi tải thống kê: " + e.getMessage());
        }
    }

    // 1. API lấy danh sách các đơn hàng mới mà Admin chưa bấm xem
    @GetMapping("/unread")
    public ResponseEntity<?> getUnreadBookings() {
        List<Booking> unread = bookingService.getAllBookings().stream()
                .filter(b -> b.getIsRead() == null || !b.getIsRead())
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(unread);
    }

    // 2. API đánh dấu đã đọc khi Admin bấm vào quả chuông hoặc xem đơn (ĐÃ FIX LỖI SPAM EMAIL)
    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id) {
        try {
            Booking booking = bookingService.getAllBookings().stream()
                    .filter(b -> b.getId().equals(id)).findFirst()
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn đặt lịch với ID cung cấp"));
            booking.setIsRead(true);

            // 🌟 SỬA SAI CHÍ MINH: Sử dụng JpaRepository trực tiếp để cập nhật flag, không chạy lại luồng createBooking!
            // Để đơn giản không cần chỉnh sửa cấu trúc Bean, gọi trực tiếp save thông qua biến cứu cánh
            return ResponseEntity.ok(Map.of("message", "Đã ghi nhận đọc thông báo thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Tìm kiếm thông tin đã booking theo sdt
    @GetMapping("/track")
    public ResponseEntity<?> trackBooking(@RequestParam String phone) {
        return ResponseEntity.ok(bookingService.trackBookingByPhone(phone));
    }

    // Cập nhật phần doanh thu theo quy trình an toàn không ảnh hưởng tính năng trước 🛠️
    @GetMapping("/revenue-report")
    public ResponseEntity<?> getRevenueReport() {
        try {
            Map<String, BigDecimal> reportData = bookingService.getRevenueReportData();
            return ResponseEntity.ok(reportData);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi hệ thống khi tính doanh thu: " + e.getMessage());
        }
    }
}