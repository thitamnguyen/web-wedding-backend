package com.example.demo.controller;

import com.example.demo.model.WeddingService;
import com.example.demo.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wedding-services")
@CrossOrigin(origins = "*") // Cho phép React gọi API công khai
public class ServiceController {

    @Autowired
    private ServiceRepository serviceRepository;

    // 1. Lấy toàn bộ danh sách gói dịch vụ
    @GetMapping
    public List<WeddingService> getAllServices() {
        return serviceRepository.findAll();
    }

    // 2. Lấy chi tiết một gói dịch vụ theo ID
    @GetMapping("/{id}")
    public WeddingService getServiceById(@PathVariable Long id) {
        return serviceRepository.findById(id).orElse(null);
    }

    // 3. API Admin: Thêm mới một gói dịch vụ cưới (Đã xóa hàm trùng lặp)
    @PostMapping("/admin/add")
    public ResponseEntity<?> addService(@RequestBody WeddingService service) {
        // Tự động map tất cả các trường bao gồm cả imageUrl từ Frontend sang và lưu vào DB
        WeddingService savedService = serviceRepository.save(service);
        return ResponseEntity.ok(savedService);
    }

    // 4. API Admin: Cập nhật / Sửa gói dịch vụ theo ID (Đã bổ sung set BẢO LƯU ẢNH)
    @PutMapping("/admin/update/{id}")
    public ResponseEntity<WeddingService> updateService(@PathVariable Long id, @RequestBody WeddingService serviceDetails) {
        WeddingService existingService = serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy gói dịch vụ với ID: " + id));

        // Cập nhật các thông tin cơ bản
        existingService.setTitle(serviceDetails.getTitle());
        existingService.setShortDescription(serviceDetails.getShortDescription());
        existingService.setPriceRange(serviceDetails.getPriceRange());
        existingService.setIconName(serviceDetails.getIconName());

        // 👉 ĐÂY LÀ DÒNG BỔ SUNG: Giúp cập nhật hình ảnh khi Admin chỉnh sửa gói
        existingService.setImageUrl(serviceDetails.getImageUrl());
        // Thêm dòng này vào ngay dưới existingService.setImageUrl(...)
        existingService.setDetailedDescription(serviceDetails.getDetailedDescription());

        WeddingService updatedService = serviceRepository.save(existingService);
        return ResponseEntity.ok(updatedService);
    }

    // 5. API Admin: Xóa gói dịch vụ theo ID
    @DeleteMapping("/admin/delete/{id}")
    public ResponseEntity<?> deleteService(@PathVariable Long id) {
        WeddingService existingService = serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy gói dịch vụ với ID: " + id));

        serviceRepository.delete(existingService);
        return ResponseEntity.ok("Đã xóa thành công gói dịch vụ có ID: " + id);
    }
}