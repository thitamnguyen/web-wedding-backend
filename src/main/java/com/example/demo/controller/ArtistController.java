package com.example.demo.controller;

import com.example.demo.model.Profile;
import com.example.demo.model.ProductItem;
import com.example.demo.repository.ProfileRepository;
import com.example.demo.repository.ProductItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping
    public List<Profile> getAllArtists() {
        return profileRepository.findAll();
    }

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

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createPhotographer(@RequestBody Profile profile) {
        try {
            if (profile.getUserId() == null) {
                Long maxId = profileRepository.findAll().stream()
                        .map(p -> p.getUserId() != null ? p.getUserId() : 0L)
                        .max(Long::compare)
                        .orElse(0L);
                profile.setUserId(maxId + 1);
            }

            if (profile.getExperienceYears() == null) profile.setExperienceYears(3);
            if (profile.getRating() == null) profile.setRating(new BigDecimal("5.0"));
            if (profile.getReviewCount() == null) profile.setReviewCount(0);

            Profile savedProfile = profileRepository.save(profile);
            return ResponseEntity.ok(savedProfile);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Loi he thong: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
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

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deletePhotographer(@PathVariable Long id) {
        return profileRepository.findById(id)
                .map(profile -> {
                    profileRepository.delete(profile);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
