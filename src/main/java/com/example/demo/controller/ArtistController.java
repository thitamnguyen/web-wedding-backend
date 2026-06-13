package com.example.demo.controller;


import com.example.demo.model.Profile;
import com.example.demo.model.ProductItem;
import com.example.demo.repository.ProfileRepository;
import com.example.demo.repository.ProductItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/artists")
@CrossOrigin(origins = "http://localhost:5173") // Cho phép React truy cập
public class ArtistController {

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private ProductItemRepository productItemRepository;

    // API lấy danh sách cho Trang Chủ
    @GetMapping
    public List<Profile> getAllArtists() {
        return profileRepository.findAll();
    }


    // API lấy chi tiết 1 người (cho trang ArtistDetail)
    @GetMapping("/{id}")
    public ResponseEntity<Profile> getArtistById(@PathVariable Long id) {
        return profileRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/works")
    public List<ProductItem> getArtistWorks(@PathVariable Long id) {
        return productItemRepository.findByPhotographerIdAndPublishedTrueOrderByPublishedAtDesc(id);
    }
}
