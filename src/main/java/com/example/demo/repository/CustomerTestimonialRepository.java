package com.example.demo.repository;

import com.example.demo.model.CustomerTestimonial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerTestimonialRepository extends JpaRepository<CustomerTestimonial, Long> {
    List<CustomerTestimonial> findByPublishedTrueOrderByCreatedAtDesc();
}
