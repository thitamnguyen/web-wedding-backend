package com.example.demo.controller;

import com.example.demo.model.Profile;
import com.example.demo.model.ProductItem;
import com.example.demo.repository.ProfileRepository;
import com.example.demo.repository.ProductItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/artists")
@CrossOrigin(origins = "http://localhost:5173")
public class ArtistController {

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private ProductItemRepository productItemRepository;

    // 1. GET ALL ARTISTS
    @GetMapping
    public List<Profile> getAllArtists() {
        return profileRepository.findAll();
    }

    // 2. GET ARTIST BY ID
    @GetMapping("/{id}")
    public ResponseEntity<Profile> getArtistById(@PathVariable Long id) {
        return profileRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 3. GET WORKS BY ARTIST ID
    @GetMapping("/{id}/works")
    public List<ProductItem> getArtistWorks(@PathVariable Long id) {
        return productItemRepository.findByPhotographerIdAndPublishedTrueOrderByPublishedAtDesc(id);
    }

    // 4. POST - THÊM MỚI (ĐÃ ĐỒNG BỘ THEO BIẾN USER_ID CỦA ENTITY 🛠️)
    @PostMapping
    public ResponseEntity<?> createPhotographer(@RequestBody Profile profile) {
        try {
            // Giải pháp gán ID thủ công phòng tránh lỗi Identifier must be manually assigned
            if (profile.getUserId() == null) {
                Long maxId = profileRepository.findAll().stream()
                        .map(p -> p.getUserId() != null ? p.getUserId() : 0L)
                        .max(Long::compare)
                        .orElse(0L);
                profile.setUserId(maxId + 1);
            }

            // Điền giá trị mặc định cho các cột số để dữ liệu lưu xuống DB trông đẹp hơn
            if (profile.getExperienceYears() == null) profile.setExperienceYears(3);
            if (profile.getRating() == null) profile.setRating(new BigDecimal("5.0"));
            if (profile.getReviewCount() == null) profile.setReviewCount(0);

            Profile savedProfile = profileRepository.save(profile);
            return ResponseEntity.ok(savedProfile);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi hệ thống: " + e.getMessage());
        }
    }

    // 5. PUT - CẬP NHẬT THEO USER_ID
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePhotographer(@PathVariable Long id, @RequestBody Profile profileDetails) {
        return profileRepository.findById(id)
                .map(existingProfile -> {
                    existingProfile.setFullName(profileDetails.getFullName());
                    existingProfile.setJobTitle(profileDetails.getJobTitle());
                    existingProfile.setAvatarUrl(profileDetails.getAvatarUrl());

                    Profile updatedProfile = profileRepository.save(existingProfile);
                    return ResponseEntity.ok(updatedProfile);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // 6. DELETE - XÓA THEO USER_ID
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePhotographer(@PathVariable Long id) {
        return profileRepository.findById(id)
                .map(profile -> {
                    profileRepository.delete(profile);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}