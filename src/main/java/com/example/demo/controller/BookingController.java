package com.example.demo.controller;

import com.example.demo.model.Booking;
import com.example.demo.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @PostMapping("/create")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<?> createBooking(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody Booking booking) {
        try {
            if (booking.getUserId() == null) {
                Long userId = extractUserId(authorization);
                if (userId != null) {
                    booking.setUserId(userId);
                }
            }
            Booking savedBooking = bookingService.createBooking(booking);
            return ResponseEntity.ok(savedBooking);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Loi khi dat lich: " + e.getMessage());
        }
    }

    private Long extractUserId(String authorization) {
        if (authorization == null || authorization.isBlank()) {
            return null;
        }

        String token = authorization.trim();
        if (token.toLowerCase(Locale.ROOT).startsWith("bearer ")) {
            token = token.substring(7).trim();
        }

        if (!token.startsWith("authenticated-")) {
            return null;
        }

        try {
            return Long.parseLong(token.substring("authenticated-".length()));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateBookingStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        try {
            Booking updatedBooking = bookingService.updateStatus(id, status);
            return ResponseEntity.ok(updatedBooking);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Loi cap nhat trang thai: " + e.getMessage());
        }
    }

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
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getDashboardStats() {
        try {
            return ResponseEntity.ok(bookingService.getDashboardStats());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Loi tai thong ke: " + e.getMessage());
        }
    }

    @GetMapping("/unread")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUnreadBookings() {
        List<Booking> unread = bookingService.getAllBookings().stream()
                .filter(b -> b.getIsRead() == null || !b.getIsRead())
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(unread);
    }

    @PutMapping("/{id}/read")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> markAsRead(@PathVariable Long id) {
        try {
            Booking booking = bookingService.getAllBookings().stream()
                    .filter(b -> b.getId().equals(id))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Khong tim thay don dat lich voi ID cung cap"));
            booking.setIsRead(true);
            return ResponseEntity.ok(Map.of("message", "Da ghi nhan doc thong bao thanh cong"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/track")
    public ResponseEntity<?> trackBooking(@RequestParam String phone) {
        return ResponseEntity.ok(bookingService.trackBookingByPhone(phone));
    }

    @GetMapping("/revenue-report")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getRevenueReport() {
        try {
            Map<String, BigDecimal> reportData = bookingService.getRevenueReportData();
            return ResponseEntity.ok(reportData);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Loi he thong khi tinh doanh thu: " + e.getMessage());
        }
    }
}
