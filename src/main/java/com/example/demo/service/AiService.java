package com.example.demo.service;

import com.example.demo.dto.AiResponse;
import com.example.demo.dto.TryOnResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class AiService {

    private final RestTemplate restTemplate =
            new RestTemplate();

    @Value("${ai.tryon.real-url:}")
    private String realTryOnUrl;

    public AiResponse predictBodyShape(
            MultipartFile imageFile
    ) throws IOException {

        String url =
                "http://127.0.0.1:8000/predict";

        HttpHeaders headers =
                new HttpHeaders();

        headers.setContentType(
                MediaType.MULTIPART_FORM_DATA
        );

        MultiValueMap<String, Object> body =
                new LinkedMultiValueMap<>();

        body.add(
                "file",
                new ByteArrayResource(
                        imageFile.getBytes()
                ) {

                    @Override
                    public String getFilename() {
                        return imageFile.getOriginalFilename();
                    }
                }
        );

        HttpEntity<
                        MultiValueMap<String, Object>
                        > requestEntity =
                new HttpEntity<>(
                        body,
                        headers
                );

        ResponseEntity<AiResponse>
                response =
                restTemplate.postForEntity(
                        url,
                        requestEntity,
                        AiResponse.class
                );

        return response.getBody();
    }

    public TryOnResult generateWeddingTryOn(
            MultipartFile personFile,
            MultipartFile garmentFile,
            String role,
            String style,
            String mode
    ) throws IOException {

        String url =
                "http://127.0.0.1:8000/try-on";

        HttpHeaders headers =
                new HttpHeaders();

        headers.setContentType(
                MediaType.MULTIPART_FORM_DATA
        );

        MultiValueMap<String, Object> body =
                new LinkedMultiValueMap<>();

        body.add(
                "person",
                new ByteArrayResource(
                        personFile.getBytes()
                ) {

                    @Override
                    public String getFilename() {
                        return personFile.getOriginalFilename();
                    }
                }
        );

        body.add(
                "garment",
                new ByteArrayResource(
                        garmentFile.getBytes()
                ) {

                    @Override
                    public String getFilename() {
                        return garmentFile.getOriginalFilename();
                    }
                }
        );

        body.add(
                "role",
                role == null ? "bride" : role
        );

        body.add(
                "style",
                style == null ? "studio" : style
        );

        body.add(
                "mode",
                mode == null ? "preview" : mode
        );

        HttpEntity<
                        MultiValueMap<String, Object>
                        > requestEntity =
                new HttpEntity<>(
                        body,
                        headers
                );

        boolean wantsRealMode =
                "real".equalsIgnoreCase(mode);

        String targetUrl =
                (wantsRealMode
                        && realTryOnUrl != null
                        && !realTryOnUrl.isBlank())
                        ? realTryOnUrl
                        : url;

        ResponseEntity<byte[]>
                response =
                restTemplate.postForEntity(
                        targetUrl,
                        requestEntity,
                        byte[].class
                );

        String modeHeader =
                response.getHeaders().getFirst("X-Try-On-Mode");

        String noticeHeader =
                response.getHeaders().getFirst("X-Try-On-Notice");

        String resolvedMode =
                modeHeader != null
                        ? modeHeader
                        : (wantsRealMode ? "real" : "preview");

        String resolvedNotice =
                noticeHeader != null
                        ? noticeHeader
                        : (wantsRealMode
                        ? "real"
                        : "preview");

        return new TryOnResult(
                response.getBody(),
                resolvedMode,
                resolvedNotice
        );
    }
}
