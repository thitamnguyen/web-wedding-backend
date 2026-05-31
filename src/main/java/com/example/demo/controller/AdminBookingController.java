package com.example.demo.controller;

import com.example.demo.model.Booking;
import com.example.demo.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/bookings")
@CrossOrigin(origins = "http://localhost:5173") // Cho phép React truy cập
public class AdminBookingController {

    @Autowired
    private BookingRepository bookingRepository;

    // 1. API Lấy toàn bộ danh sách lịch đặt cưới
    @GetMapping
    public ResponseEntity<List<Booking>> getAllBookings() {
        List<Booking> bookings = bookingRepository.findAll();
        return ResponseEntity.ok(bookings);
    }

    // 2. API Cập nhật trạng thái duyệt lịch (CONFIRMED, DONE, CANCELLED)
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> request) {
        Optional<Booking> bookingOpt = bookingRepository.findById(id);

        if (bookingOpt.isPresent()) {
            Booking booking = bookingOpt.get();
            String newStatus = request.get("status");

            booking.setStatus(newStatus);
            bookingRepository.save(booking);

            return ResponseEntity.ok(Map.of("message", "Cập nhật trạng thái lịch hẹn thành công!"));
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Không tìm thấy lịch đặt!"));
    }
}
