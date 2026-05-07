package com.example.demo.controller;

import com.example.demo.model.WeddingService;
import com.example.demo.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wedding-services")
@CrossOrigin(origins = "*") // Cho phép React gọi API
public class ServiceController {

    @Autowired
    private ServiceRepository serviceRepository;

    @GetMapping
    public List<WeddingService> getAllServices() {
        return serviceRepository.findAll();
    }

    @GetMapping("/{id}")
    public WeddingService getServiceById(@PathVariable Long id) {
        return serviceRepository.findById(id).orElse(null);
    }
}