package com.example.demo.controller;

import com.example.demo.model.MakeupArtist;
import com.example.demo.model.ProductItem;
import com.example.demo.repository.MakeupArtistRepository;
import com.example.demo.repository.ProductItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/makeup-artists")
@CrossOrigin("*")
public class MakeupController {

    @Autowired
    private MakeupArtistRepository makeupArtistRepository;

    @Autowired
    private ProductItemRepository productItemRepository;

    // 1. LẤY DANH SÁCH (GET)
    @GetMapping
    public List<MakeupArtist> getAllMakeupArtists() {
        return makeupArtistRepository.findAll();
    }

    // 2. LẤY THEO ID (GET)
    @GetMapping("/{id}")
    public ResponseEntity<MakeupArtist> getMakeupArtistById(@PathVariable Long id) {
        return makeupArtistRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 3. LẤY SẢN PHẨM ĐÃ LÀM (GET)
    @GetMapping("/{id}/works")
    public List<ProductItem> getMakeupArtistWorks(@PathVariable Long id) {
        return productItemRepository.findByMakeupArtistIdAndPublishedTrueOrderByPublishedAtDesc(id);
    }

    // 4. CHỨC NĂNG THÊM MỚI (POST)
    @PostMapping
    public ResponseEntity<MakeupArtist> createMakeupArtist(@RequestBody MakeupArtist artist) {
        try {
            MakeupArtist savedArtist = makeupArtistRepository.save(artist);
            return ResponseEntity.ok(savedArtist);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    // 5. CHỨC NĂNG SỬA (PUT) - ĐÃ HẾT LỖI BIÊN DỊCH 🔴
    @PutMapping("/{id}")
    public ResponseEntity<MakeupArtist> updateMakeupArtist(@PathVariable Long id, @RequestBody MakeupArtist artistDetails) {
        return makeupArtistRepository.findById(id)
                .map(existingArtist -> {
                    // Sử dụng chuẩn camelCase để map chính xác với Getter/Setter của Java Entity
                    existingArtist.setFullName(artistDetails.getFullName());
                    existingArtist.setJobTitle(artistDetails.getJobTitle());
                    existingArtist.setAvatarUrl(artistDetails.getAvatarUrl());

                    // Nếu trong Model MakeupArtist của em có trường phone, hãy giữ dòng dưới. Ngược lại thì xóa đi.
                    // existingArtist.setPhone(artistDetails.getPhone());

                    MakeupArtist updatedArtist = makeupArtistRepository.save(existingArtist);
                    return ResponseEntity.ok(updatedArtist);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // 6. CHỨC NĂNG XÓA (DELETE)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMakeupArtist(@PathVariable Long id) {
        return makeupArtistRepository.findById(id)
                .map(artist -> {
                    makeupArtistRepository.delete(artist);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}