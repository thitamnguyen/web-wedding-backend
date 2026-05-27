package com.example.demo.repository;

import com.example.demo.model.WeddingDress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WeddingDressRepository
        extends JpaRepository<
        WeddingDress,
        Long> {

    List<WeddingDress>
    findByBodyShape(
            String bodyShape
    );
}
