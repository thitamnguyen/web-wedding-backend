package com.example.demo.controller;

import com.example.demo.model.CustomerTestimonial;
import com.example.demo.repository.CustomerTestimonialRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/testimonials")
@CrossOrigin(origins = "*")
public class CustomerTestimonialController {

    private final CustomerTestimonialRepository repository;

    public CustomerTestimonialController(CustomerTestimonialRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<CustomerTestimonial> getTestimonials() {
        return repository.findByPublishedTrueOrderByCreatedAtDesc();
    }
}
