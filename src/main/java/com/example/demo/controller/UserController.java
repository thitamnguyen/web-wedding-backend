package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
// BƯỚC QUAN TRỌNG: Mở khóa CORS cho Frontend (React thường chạy cổng 3000)
@CrossOrigin(origins = "http://localhost:3000")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    // API để lấy danh sách tất cả người dùng
    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll(); // Tự động lấy data từ MySQL trả về dạng JSON
    }
}