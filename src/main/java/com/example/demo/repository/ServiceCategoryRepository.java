package com.example.demo.repository;


import com.example.demo.model.ServiceCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceCategoryRepository
        extends JpaRepository<ServiceCategory, Integer> {
}