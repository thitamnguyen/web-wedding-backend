package com.example.demo.controller;

import com.example.demo.model.Booking;
import com.example.demo.repository.BookingRepository;
import com.example.demo.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> request) {
        Optional<Booking> bookingOpt = bookingRepository.findById(id);

        if (bookingOpt.isPresent()) {
            Booking booking = bookingOpt.get();
            String newStatus = request.get("status");

            booking.setStatus(newStatus);
            bookingRepository.save(booking);

            return ResponseEntity.ok(Map.of("message", "Cap nhat trang thai lich hen thanh cong!"));
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Khong tim thay lich dat!"));
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
