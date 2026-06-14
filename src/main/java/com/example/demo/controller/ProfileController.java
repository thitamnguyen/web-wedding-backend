package com.example.demo.controller;

import com.example.demo.dto.ChangePasswordRequest;
import com.example.demo.dto.ProfileDashboardResponse;
import com.example.demo.dto.ProfileUpdateRequest;
import com.example.demo.dto.ProfileUserDto;
import com.example.demo.service.ProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/profile")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
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
                    "message", "Cập nhật thông tin thành công",
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
                    "message", "Đổi mật khẩu thành công",
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
        return "Chưa đăng nhập".equals(message) || "Không tìm thấy người dùng".equals(message);
    }
}
