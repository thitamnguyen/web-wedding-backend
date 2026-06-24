package com.example.demo.controller;

import com.example.demo.dto.ImageUploadResult;
import com.example.demo.dto.StaffUpsertRequest;
import com.example.demo.model.MakeupArtist;
import com.example.demo.model.MakeupPortfolio;
import com.example.demo.model.ProductItem;
import com.example.demo.repository.MakeupArtistRepository;
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

import java.util.List;

@RestController
@RequestMapping("/api/makeup-artists")
@CrossOrigin("*")
public class MakeupController {

    private final MakeupArtistRepository makeupArtistRepository;
    private final ProductItemRepository productItemRepository;
    private final CloudinaryService cloudinaryService;

    public MakeupController(
            MakeupArtistRepository makeupArtistRepository,
            ProductItemRepository productItemRepository,
            CloudinaryService cloudinaryService
    ) {
        this.makeupArtistRepository = makeupArtistRepository;
        this.productItemRepository = productItemRepository;
        this.cloudinaryService = cloudinaryService;
    }

    @GetMapping
    public List<MakeupArtist> getAllMakeupArtists() {
        return makeupArtistRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<MakeupArtist> getMakeupArtistById(@PathVariable Long id) {
        return makeupArtistRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/works")
    public List<ProductItem> getMakeupArtistWorks(@PathVariable Long id) {
        return productItemRepository.findByMakeupArtistIdAndPublishedTrueOrderByPublishedAtDesc(id);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MakeupArtist> createMakeupArtist(@ModelAttribute StaffUpsertRequest request) {
        try {
            MakeupArtist artist = new MakeupArtist();
            applyRequest(artist, request, null);
            if (artist.getTotalRevenue() == null) {
                artist.setTotalRevenue(java.math.BigDecimal.ZERO);
            }
            MakeupArtist savedArtist = makeupArtistRepository.save(artist);
            return ResponseEntity.ok(savedArtist);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MakeupArtist> updateMakeupArtist(@PathVariable Long id, @ModelAttribute StaffUpsertRequest request) {
        return makeupArtistRepository.findById(id)
                .map(existingArtist -> {
                    String oldPublicId = existingArtist.getPublicId();
                    applyRequest(existingArtist, request, oldPublicId);
                    MakeupArtist saved = makeupArtistRepository.save(existingArtist);
                    if (request.getAvatarFile() != null && !request.getAvatarFile().isEmpty() && oldPublicId != null && !oldPublicId.isBlank()) {
                        cloudinaryService.deleteImage(oldPublicId);
                    }
                    return ResponseEntity.ok(saved);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteMakeupArtist(@PathVariable Long id) {
        return makeupArtistRepository.findById(id)
                .map(artist -> {
                    cloudinaryService.deleteImage(artist.getPublicId());
                    if (artist.getPortfolios() != null) {
                        for (MakeupPortfolio portfolio : artist.getPortfolios()) {
                            cloudinaryService.deleteImage(portfolio.getPublicId());
                        }
                    }
                    makeupArtistRepository.delete(artist);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private void applyRequest(MakeupArtist artist, StaffUpsertRequest request, String existingPublicId) {
        artist.setFullName(request.getFullName());
        artist.setJobTitle(request.getJobTitle());

        ImageUploadResult uploadResult = cloudinaryService.uploadImage(request.getAvatarFile());
        if (uploadResult != null) {
            artist.setAvatarUrl(uploadResult.getSecureUrl());
            artist.setPublicId(uploadResult.getPublicId());
        } else if (existingPublicId != null) {
            artist.setPublicId(existingPublicId);
        }
    }
}
