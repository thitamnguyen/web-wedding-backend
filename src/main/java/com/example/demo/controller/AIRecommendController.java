package com.example.demo.controller;

import com.example.demo.dto.AiResponse;
import com.example.demo.model.WeddingDress;
import com.example.demo.repository.WeddingDressRepository;
import com.example.demo.service.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/ai")
public class AIRecommendController {

    @Autowired
    private AiService aiService;

    @Autowired
    private WeddingDressRepository
            weddingDressRepository;

    @PostMapping("/recommend")
    public ResponseEntity<?>
    recommendDress(

            @RequestParam("file")
            MultipartFile file

    ) throws IOException {

        AiResponse aiResult =
                aiService
                        .predictBodyShape(file);

        String bodyShape =
                aiResult.getBody_shape();

        List<WeddingDress>
                dresses =

                weddingDressRepository
                        .findByBodyShape(
                                bodyShape
                        );

        return ResponseEntity.ok(
                dresses
        );
    }
}