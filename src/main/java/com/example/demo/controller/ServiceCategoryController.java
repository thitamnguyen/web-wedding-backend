package com.example.demo.controller;

import com.example.demo.dto.ServiceCategoryResponse;
import com.example.demo.dto.ServiceCategoryUpsertRequest;
import com.example.demo.dto.ServicePackageResponse;
import com.example.demo.dto.ServicePackageUpsertRequest;
import com.example.demo.service.ServiceCategoryService;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/services")
@CrossOrigin(origins = "http://localhost:5173")
public class ServiceCategoryController {

    private final ServiceCategoryService service;

    public ServiceCategoryController(ServiceCategoryService service) {
        this.service = service;
    }

    @GetMapping
    public List<ServiceCategoryResponse> getAllServices() {
        return service.getAllServices();
    }

    @GetMapping("/admin/categories")
    @PreAuthorize("hasRole('ADMIN')")
    public List<ServiceCategoryResponse> getAdminCategories() {
        return service.getAdminCategories();
    }

    @GetMapping("/admin/packages")
    @PreAuthorize("hasRole('ADMIN')")
    public List<ServicePackageResponse> getAdminPackages() {
        return service.getAdminPackages();
    }

    @PostMapping(value = "/admin/categories", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ServiceCategoryResponse createCategory(@ModelAttribute ServiceCategoryUpsertRequest request) {
        return service.createCategory(request);
    }

    @PutMapping(value = "/admin/categories/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ServiceCategoryResponse updateCategory(
            @PathVariable Integer id,
            @ModelAttribute ServiceCategoryUpsertRequest request
    ) {
        return service.updateCategory(id, request);
    }

    @DeleteMapping("/admin/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteCategory(@PathVariable Integer id) {
        service.deleteCategory(id);
    }

    @PostMapping("/admin/packages")
    @PreAuthorize("hasRole('ADMIN')")
    public ServicePackageResponse createPackage(@RequestBody ServicePackageUpsertRequest request) {
        return service.createPackage(request);
    }

    @PutMapping("/admin/packages/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ServicePackageResponse updatePackage(
            @PathVariable Integer id,
            @RequestBody ServicePackageUpsertRequest request
    ) {
        return service.updatePackage(id, request);
    }

    @DeleteMapping("/admin/packages/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deletePackage(@PathVariable Integer id) {
        service.deletePackage(id);
    }
}
