package com.example.demo.controller;

import com.example.demo.dto.ImageUploadResult;
import com.example.demo.dto.WeddingServiceUpsertRequest;
import com.example.demo.model.WeddingService;
import com.example.demo.repository.ServiceRepository;
import com.example.demo.service.CloudinaryService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/wedding-services")
@CrossOrigin(origins = "*")
public class ServiceController {

    private final ServiceRepository serviceRepository;
    private final CloudinaryService cloudinaryService;

    public ServiceController(ServiceRepository serviceRepository, CloudinaryService cloudinaryService) {
        this.serviceRepository = serviceRepository;
        this.cloudinaryService = cloudinaryService;
    }

    @GetMapping
    public List<WeddingService> getAllServices() {
        return serviceRepository.findAll();
    }

    @GetMapping("/{id}")
    public WeddingService getServiceById(@PathVariable Long id) {
        return serviceRepository.findById(id).orElse(null);
    }

    @PostMapping(value = "/admin/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> addService(@ModelAttribute WeddingServiceUpsertRequest request) {
        WeddingService service = new WeddingService();
        applyRequest(service, request, null);
        WeddingService savedService = serviceRepository.save(service);
        return ResponseEntity.ok(savedService);
    }

    @PutMapping(value = "/admin/update/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WeddingService> updateService(@PathVariable Long id, @ModelAttribute WeddingServiceUpsertRequest request) {
        WeddingService existingService = serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khong tim thay goi dich vu voi ID: " + id));

        String oldPublicId = existingService.getPublicId();
        applyRequest(existingService, request, oldPublicId);

        WeddingService updatedService = serviceRepository.save(existingService);
        if (request.getImageFile() != null && !request.getImageFile().isEmpty() && oldPublicId != null && !oldPublicId.isBlank()) {
            cloudinaryService.deleteImage(oldPublicId);
        }
        return ResponseEntity.ok(updatedService);
    }

    @DeleteMapping("/admin/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteService(@PathVariable Long id) {
        WeddingService existingService = serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khong tim thay goi dich vu voi ID: " + id));

        cloudinaryService.deleteImage(existingService.getPublicId());
        serviceRepository.delete(existingService);
        return ResponseEntity.ok("Da xoa thanh cong goi dich vu co ID: " + id);
    }

    private void applyRequest(WeddingService service, WeddingServiceUpsertRequest request, String existingPublicId) {
        service.setTitle(request.getTitle());
        service.setShortDescription(request.getShortDescription());
        service.setPriceRange(request.getPriceRange());
        service.setIconName(request.getIconName());
        service.setDetailedDescription(request.getDetailedDescription());

        ImageUploadResult uploadResult = cloudinaryService.uploadImage(request.getImageFile());
        if (uploadResult != null) {
            service.setImageUrl(uploadResult.getSecureUrl());
            service.setPublicId(uploadResult.getPublicId());
        } else if (existingPublicId != null) {
            service.setPublicId(existingPublicId);
        }
    }
}
