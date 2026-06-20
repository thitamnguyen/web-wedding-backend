package com.example.demo.controller;

import com.example.demo.model.MakeupArtist;
import com.example.demo.model.ProductItem;
import com.example.demo.repository.MakeupArtistRepository;
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

import java.util.List;

@RestController
@RequestMapping("/api/makeup-artists")
@CrossOrigin("*")
public class MakeupController {

    @Autowired
    private MakeupArtistRepository makeupArtistRepository;

    @Autowired
    private ProductItemRepository productItemRepository;

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

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MakeupArtist> createMakeupArtist(@RequestBody MakeupArtist artist) {
        try {
            MakeupArtist savedArtist = makeupArtistRepository.save(artist);
            return ResponseEntity.ok(savedArtist);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MakeupArtist> updateMakeupArtist(@PathVariable Long id, @RequestBody MakeupArtist artistDetails) {
        return makeupArtistRepository.findById(id)
                .map(existingArtist -> {
                    existingArtist.setFullName(artistDetails.getFullName());
                    existingArtist.setJobTitle(artistDetails.getJobTitle());
                    existingArtist.setAvatarUrl(artistDetails.getAvatarUrl());
                    return ResponseEntity.ok(makeupArtistRepository.save(existingArtist));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteMakeupArtist(@PathVariable Long id) {
        return makeupArtistRepository.findById(id)
                .map(artist -> {
                    makeupArtistRepository.delete(artist);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
