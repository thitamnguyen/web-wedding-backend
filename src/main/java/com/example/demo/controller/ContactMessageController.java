package com.example.demo.controller;

import com.example.demo.model.ContactMessage;
import com.example.demo.repository.ContactMessageRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
            return ResponseEntity.badRequest().body("Vui long nhap ho va ten.");
        }
        if (contactMessage.getEmail() == null || contactMessage.getEmail().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Vui long nhap email.");
        }
        if (contactMessage.getSubject() == null || contactMessage.getSubject().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Vui long nhap chu de.");
        }
        if (contactMessage.getMessage() == null || contactMessage.getMessage().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Vui long nhap noi dung lien he.");
        }

        contactMessage.setStatus("NEW");
        ContactMessage saved = contactMessageRepository.save(contactMessage);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Da gui lien he thanh cong");
        response.put("data", saved);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<ContactMessage> getAll() {
        return contactMessageRepository.findAll();
    }
}
