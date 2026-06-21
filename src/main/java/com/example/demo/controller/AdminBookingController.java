package com.example.demo.controller;

import com.example.demo.model.Booking;
import com.example.demo.repository.BookingRepository;
import com.example.demo.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/bookings")
@CrossOrigin(origins = "http://localhost:5173")
@PreAuthorize("hasRole('ADMIN')")
public class AdminBookingController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @GetMapping
    public ResponseEntity<List<Booking>> getAllBookings() {
        List<Booking> bookings = bookingRepository.findAll();
        return ResponseEntity.ok(bookings);
    }

    // HÀM ĐÃ SỬA: Hỗ trợ linh hoạt cả RequestParam (?status=) và RequestBody, xử lý dòng tiền thông minh
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long id,
            @RequestParam(value = "status", required = false) String paramStatus,
            @RequestBody(required = false) Map<String, String> requestBody) {

        Optional<Booking> bookingOpt = bookingRepository.findById(id);

        if (bookingOpt.isPresent()) {
            Booking booking = bookingOpt.get();

            // Lấy trạng thái linh hoạt từ Query Parameter hoặc từ Body JSON gửi lên
            String newStatus = paramStatus;
            if (newStatus == null && requestBody != null && requestBody.containsKey("status")) {
                newStatus = requestBody.get("status");
            }

            if (newStatus == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Trạng thái status không hợp lệ!"));
            }

            String cleanStatus = newStatus.toUpperCase().trim();
            booking.setStatus(cleanStatus);

            // Xử lý trạng thái thanh toán (paymentStatus) đồng bộ đi kèm
            if ("CONFIRMED".equals(cleanStatus)) {
                booking.setPaymentStatus("DEPOSITED"); // Ghi nhận đã thu cọc 20%
            } else if ("DONE".equals(cleanStatus)) {
                booking.setPaymentStatus("PAID_FULL");  // Ghi nhận đã thu đủ 100%
            } else if ("CANCELLED".equals(cleanStatus)) {
                booking.setPaymentStatus("CANCELLED");  // Ghi nhận đơn hủy dòng tiền
            }

            bookingRepository.save(booking);
            return ResponseEntity.ok(Map.of("message", "Cập nhật trạng thái lịch hẹn thành công!"));
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Không tìm thấy lịch đặt!"));
    }

    @GetMapping("/list")
    public ResponseEntity<Page<Booking>> getBookings(
            @RequestParam(defaultValue = "ALL") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        Page<Booking> bookingsPage = bookingService.getBookingsWithFilter(status, page, size);
        return ResponseEntity.ok(bookingsPage);
    }

    @GetMapping("/busy-dates/photographer/{id}")
    public ResponseEntity<List<java.time.LocalDate>> getPhotographerBusyDates(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.getPhotographerBusyDates(id));
    }

    @GetMapping("/busy-dates/makeup/{id}")
    public ResponseEntity<List<java.time.LocalDate>> getMakeupArtistBusyDates(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.getMakeupArtistBusyDates(id));
    }
}