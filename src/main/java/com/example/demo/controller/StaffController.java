package com.example.demo.controller;

import com.example.demo.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/staff")
@CrossOrigin(origins = "http://localhost:5173")
public class StaffController {

    @Autowired
    private BookingRepository bookingRepository;

    // Lấy lịch chụp cho Thợ ảnh
    @GetMapping("/photographer/{id}/schedule")
    public ResponseEntity<?> getPhotographerSchedule(@PathVariable Long id) {
        return ResponseEntity.ok(bookingRepository.findByPhotographerIdAndStatus(id, "CONFIRMED"));
    }

    // Lấy lịch trang điểm cho Thợ Makeup
    @GetMapping("/makeup/{id}/schedule")
    public ResponseEntity<?> getMakeupSchedule(@PathVariable Long id) {
        return ResponseEntity.ok(bookingRepository.findByMakeupArtistIdAndStatus(id, "CONFIRMED"));
    }
}