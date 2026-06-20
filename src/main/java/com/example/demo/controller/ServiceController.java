package com.example.demo.controller;

import com.example.demo.model.WeddingService;
import com.example.demo.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/wedding-services")
@CrossOrigin(origins = "*")
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

    @PostMapping("/admin/add")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> addService(@RequestBody WeddingService service) {
        WeddingService savedService = serviceRepository.save(service);
        return ResponseEntity.ok(savedService);
    }

    @PutMapping("/admin/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WeddingService> updateService(@PathVariable Long id, @RequestBody WeddingService serviceDetails) {
        WeddingService existingService = serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khong tim thay goi dich vu voi ID: " + id));

        existingService.setTitle(serviceDetails.getTitle());
        existingService.setShortDescription(serviceDetails.getShortDescription());
        existingService.setPriceRange(serviceDetails.getPriceRange());
        existingService.setIconName(serviceDetails.getIconName());
        existingService.setImageUrl(serviceDetails.getImageUrl());
        existingService.setDetailedDescription(serviceDetails.getDetailedDescription());

        WeddingService updatedService = serviceRepository.save(existingService);
        return ResponseEntity.ok(updatedService);
    }

    @DeleteMapping("/admin/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteService(@PathVariable Long id) {
        WeddingService existingService = serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khong tim thay goi dich vu voi ID: " + id));

        serviceRepository.delete(existingService);
        return ResponseEntity.ok("Da xoa thanh cong goi dich vu co ID: " + id);
    }
}
