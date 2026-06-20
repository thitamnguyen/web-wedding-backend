package com.example.demo.controller;

import com.example.demo.dto.ChangePasswordRequest;
import com.example.demo.dto.ProfileDashboardResponse;
import com.example.demo.dto.ProfileUpdateRequest;
import com.example.demo.dto.ProfileUserDto;
import com.example.demo.service.ProfileService;
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

import java.util.Map;

@RestController
@RequestMapping("/api/profile")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
@PreAuthorize("hasRole('CLIENT')")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/me")
    public ResponseEntity<ProfileDashboardResponse> getDashboard(@RequestHeader(value = "Authorization", required = false) String authorization) {
        try {
            return ResponseEntity.ok(profileService.getDashboard(authorization));
        } catch (Exception e) {
            if (isAuthError(e)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            throw e;
        }
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateProfile(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody ProfileUpdateRequest request) {
        try {
            ProfileUserDto updated = profileService.updateProfile(authorization, request);
            return ResponseEntity.ok(Map.of(
                    "message", "Cap nhat thong tin thanh cong",
                    "user", updated
            ));
        } catch (Exception e) {
            if (isAuthError(e)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", e.getMessage()
            ));
        }
    }

    @PutMapping("/me/password")
    public ResponseEntity<?> changePassword(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody ChangePasswordRequest request) {
        try {
            ProfileUserDto updated = profileService.changePassword(authorization, request);
            return ResponseEntity.ok(Map.of(
                    "message", "Doi mat khau thanh cong",
                    "user", updated
            ));
        } catch (Exception e) {
            if (isAuthError(e)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", e.getMessage()
            ));
        }
    }

    private boolean isAuthError(Exception e) {
        String message = e.getMessage();
        return "Chua dang nhap".equals(message) || "Khong tim thay nguoi dung".equals(message);
    }
}
