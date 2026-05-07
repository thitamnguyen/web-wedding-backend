package com.example.demo.controller;

import com.example.demo.model.MakeupArtist;
import com.example.demo.repository.MakeupArtistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/makeup-artists")
@CrossOrigin("*") // Để Frontend gọi được API
public class MakeupController {

    @Autowired
    private MakeupArtistRepository makeupArtistRepository;

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
}
