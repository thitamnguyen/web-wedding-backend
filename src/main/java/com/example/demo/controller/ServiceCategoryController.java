package com.example.demo.controller;

import com.example.demo.service.ServiceCategoryService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/services")
@CrossOrigin(origins = "http://localhost:5173")
public class ServiceCategoryController {

    private final ServiceCategoryService service;

    // Constructor Injection
    public ServiceCategoryController(
            ServiceCategoryService service
    ) {
        this.service = service;
    }

    @GetMapping
    public Object getAllServices() {
        return service.getAllServices();
    }
}