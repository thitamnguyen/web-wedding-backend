package com.example.demo.controller;

import com.example.demo.model.Notification;
import com.example.demo.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    @Autowired
    private NotificationRepository notificationRepository;

    // 1. Lấy tất cả thông báo cho Admin
    @GetMapping
    public List<Notification> getAllNotifications() {
        return notificationRepository.findAllByOrderByCreatedAtDesc();
    }

    // 2. Lấy số lượng thông báo chưa đọc (hiển thị Badge số trên chuông)
    @GetMapping("/unread-count")
    public ResponseEntity<?> getUnreadCount() {
        long count = notificationRepository.countByIsReadFalse();
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    // 3. Bấm vào thông báo -> Chuyển trạng thái sang Đã Đọc
    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id) {
        return notificationRepository.findById(id).map(notif -> {
            notif.setIsRead(true);
            notificationRepository.save(notif);
            return ResponseEntity.ok(Map.of("success", true, "message", "Đã đánh dấu đọc thông báo"));
        }).orElse(ResponseEntity.notFound().build());
    }

    // 4. Đánh dấu đọc tất cả nhanh
    @PutMapping("/read-all")
    public ResponseEntity<?> markAllAsRead() {
        List<Notification> unreadList = notificationRepository.findAll();
        for (Notification notif : unreadList) {
            notif.setIsRead(true);
        }
        notificationRepository.saveAll(unreadList);
        return ResponseEntity.ok(Map.of("success", true));
    }
}