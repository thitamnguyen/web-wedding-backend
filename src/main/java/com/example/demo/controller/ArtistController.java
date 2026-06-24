package com.example.demo.controller;

import com.example.demo.dto.ImageUploadResult;
import com.example.demo.dto.StaffUpsertRequest;
import com.example.demo.model.Profile;
import com.example.demo.model.ProductItem;
import com.example.demo.repository.ProfileRepository;
import com.example.demo.repository.ProductItemRepository;
import com.example.demo.service.CloudinaryService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/artists")
@CrossOrigin(origins = "http://localhost:5173")
public class ArtistController {

    private final ProfileRepository profileRepository;
    private final ProductItemRepository productItemRepository;
    private final CloudinaryService cloudinaryService;

    public ArtistController(
            ProfileRepository profileRepository,
            ProductItemRepository productItemRepository,
            CloudinaryService cloudinaryService
    ) {
        this.profileRepository = profileRepository;
        this.productItemRepository = productItemRepository;
        this.cloudinaryService = cloudinaryService;
    }

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

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createPhotographer(@ModelAttribute StaffUpsertRequest request) {
        try {
            Profile profile = new Profile();
            applyRequest(profile, request, null);

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

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updatePhotographer(@PathVariable Long id, @ModelAttribute StaffUpsertRequest request) {
        return profileRepository.findById(id)
                .map(existingProfile -> {
                    String oldPublicId = existingProfile.getPublicId();
                    applyRequest(existingProfile, request, oldPublicId);

                    Profile updatedProfile = profileRepository.save(existingProfile);
                    if (request.getAvatarFile() != null && !request.getAvatarFile().isEmpty() && oldPublicId != null && !oldPublicId.isBlank()) {
                        cloudinaryService.deleteImage(oldPublicId);
                    }
                    return ResponseEntity.ok(updatedProfile);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deletePhotographer(@PathVariable Long id) {
        return profileRepository.findById(id)
                .map(profile -> {
                    cloudinaryService.deleteImage(profile.getPublicId());
                    profileRepository.delete(profile);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private void applyRequest(Profile profile, StaffUpsertRequest request, String existingPublicId) {
        profile.setFullName(request.getFullName());
        profile.setJobTitle(request.getJobTitle());

        ImageUploadResult uploadResult = cloudinaryService.uploadImage(request.getAvatarFile());
        if (uploadResult != null) {
            profile.setAvatarUrl(uploadResult.getSecureUrl());
            profile.setPublicId(uploadResult.getPublicId());
        } else if (existingPublicId != null) {
            profile.setPublicId(existingPublicId);
        }
    }
}
