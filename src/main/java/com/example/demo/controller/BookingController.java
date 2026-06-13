package com.example.demo.controller;

import com.example.demo.model.Booking;
import com.example.demo.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @PostMapping("/create")
    public ResponseEntity<?> createBooking(@RequestBody Booking booking) {
        try {
            Booking savedBooking = bookingService.createBooking(booking);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đặt lịch thành công.");
            response.put("booking", savedBooking);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException exception) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", exception.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception exception) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi khi đặt lịch: " + exception.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateBookingStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        try {
            Booking updatedBooking = bookingService.updateStatus(id, status);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cập nhật trạng thái thành công.");
            response.put("booking", updatedBooking);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException exception) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", exception.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception exception) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi cập nhật trạng thái: " + exception.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
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
}
