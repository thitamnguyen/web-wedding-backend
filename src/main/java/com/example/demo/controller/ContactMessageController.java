package com.example.demo.controller;

import com.example.demo.model.ContactMessage;
import com.example.demo.repository.ContactMessageRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/contact-messages")
@CrossOrigin(origins = "*")
public class ContactMessageController {

    private final ContactMessageRepository contactMessageRepository;

    public ContactMessageController(ContactMessageRepository contactMessageRepository) {
        this.contactMessageRepository = contactMessageRepository;
    }

    @PostMapping
    public ResponseEntity<?> submit(@RequestBody ContactMessage contactMessage) {
        if (contactMessage.getFullName() == null || contactMessage.getFullName().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Vui lòng nhập họ và tên.");
        }
        if (contactMessage.getEmail() == null || contactMessage.getEmail().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Vui lòng nhập email.");
        }
        if (contactMessage.getSubject() == null || contactMessage.getSubject().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Vui lòng nhập chủ đề.");
        }
        if (contactMessage.getMessage() == null || contactMessage.getMessage().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Vui lòng nhập nội dung liên hệ.");
        }

        contactMessage.setStatus("NEW");
        ContactMessage saved = contactMessageRepository.save(contactMessage);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Đã gửi liên hệ thành công");
        response.put("data", saved);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public List<ContactMessage> getAll() {
        return contactMessageRepository.findAll();
    }
}
