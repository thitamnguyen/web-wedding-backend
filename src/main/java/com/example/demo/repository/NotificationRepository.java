package com.example.demo.repository;

import com.example.demo.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List; // Import đúng thư viện List ở đây

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Thầy đã sửa: Trả về List<Notification> viết chuẩn, xếp thứ tự mới nhất lên đầu
    List<Notification> findAllByOrderByCreatedAtDesc();

    // Đếm số lượng thông báo chưa đọc
    long countByIsReadFalse();
}