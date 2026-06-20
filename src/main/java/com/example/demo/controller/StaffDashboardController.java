package com.example.demo.controller;

import com.example.demo.dto.StaffDashboardResponse;
import com.example.demo.dto.StaffProfileDto;
import com.example.demo.dto.StaffProfileUpdateRequest;
import com.example.demo.dto.StaffRevenuePointDto;
import com.example.demo.dto.StaffScheduleDto;
import com.example.demo.dto.StaffWorkDto;
import com.example.demo.service.StaffService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/staff")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
@PreAuthorize("hasRole('STAFF')")
public class StaffDashboardController {

    private final StaffService staffService;

    public StaffDashboardController(StaffService staffService) {
        this.staffService = staffService;
    }

    @GetMapping("/me/dashboard")
    public ResponseEntity<?> getDashboard(@RequestHeader(value = "Authorization", required = false) String authorization) {
        try {
            StaffDashboardResponse response = staffService.getDashboard(authorization);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return authOrBadRequest(e);
        }
    }

    @GetMapping("/me/profile")
    public ResponseEntity<?> getProfile(@RequestHeader(value = "Authorization", required = false) String authorization) {
        try {
            StaffProfileDto response = staffService.getProfile(authorization);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return authOrBadRequest(e);
        }
    }

    @PutMapping("/me/profile")
    public ResponseEntity<?> updateProfile(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody StaffProfileUpdateRequest request
    ) {
        try {
            StaffProfileDto response = staffService.updateProfile(authorization, request);
            return ResponseEntity.ok(Map.of(
                    "message", "Cap nhat thong tin nhan vien thanh cong",
                    "profile", response
            ));
        } catch (Exception e) {
            return authOrBadRequest(e);
        }
    }

    @GetMapping("/me/schedule")
    public ResponseEntity<?> getSchedule(@RequestHeader(value = "Authorization", required = false) String authorization) {
        try {
            List<StaffScheduleDto> response = staffService.getSchedule(authorization);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return authOrBadRequest(e);
        }
    }

    @GetMapping("/me/works")
    public ResponseEntity<?> getWorks(@RequestHeader(value = "Authorization", required = false) String authorization) {
        try {
            List<StaffWorkDto> response = staffService.getWorks(authorization);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return authOrBadRequest(e);
        }
    }

    @GetMapping("/me/revenue")
    public ResponseEntity<?> getRevenue(@RequestHeader(value = "Authorization", required = false) String authorization) {
        try {
            List<StaffRevenuePointDto> response = staffService.getRevenue(authorization);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return authOrBadRequest(e);
        }
    }

    private ResponseEntity<?> authOrBadRequest(Exception e) {
        String message = e.getMessage() == null ? "Loi khong xac dinh" : e.getMessage();
        if ("Chua dang nhap".equals(message) || message.contains("khong co quyen nhan vien") || "Khong tim thay nguoi dung".equals(message)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", message));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", message));
    }
}
