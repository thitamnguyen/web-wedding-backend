package com.example.demo.service;

import com.example.demo.dto.AiResponse;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class AiService {

    private final RestTemplate restTemplate = new RestTemplate();

    public AiResponse predictBodyShape(MultipartFile imageFile) throws IOException {

        String url = "http://127.0.0.1:8000/predict";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        body.add(
                "file",
                new ByteArrayResource(imageFile.getBytes()) {
                    @Override
                    public String getFilename() {
                        return imageFile.getOriginalFilename();
                    }
                }
        );

        HttpEntity<MultiValueMap<String, Object>> requestEntity =
                new HttpEntity<>(body, headers);

        try {

            ResponseEntity<AiResponse> response =
                    restTemplate.postForEntity(
                            url,
                            requestEntity,
                            AiResponse.class
                    );

            return response.getBody();

        } catch (HttpStatusCodeException e) {

            // Trả lại lỗi từ FastAPI
            throw new RuntimeException(e.getResponseBodyAsString());

        } catch (Exception e) {

            throw new RuntimeException("Không thể kết nối tới AI Server.");

        }
    }
}