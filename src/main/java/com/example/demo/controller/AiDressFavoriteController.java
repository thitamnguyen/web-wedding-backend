package com.example.demo.controller;

import com.example.demo.service.AiDressFavoriteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/ai-favorites")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
@PreAuthorize("hasRole('CLIENT')")
public class AiDressFavoriteController {

    private final AiDressFavoriteService aiDressFavoriteService;

    public AiDressFavoriteController(AiDressFavoriteService aiDressFavoriteService) {
        this.aiDressFavoriteService = aiDressFavoriteService;
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMyFavorites(@RequestHeader(value = "Authorization", required = false) String authorization) {
        Long userId = resolveUserId(authorization);
        return ResponseEntity.ok(aiDressFavoriteService.getFavoriteDressIdsForUser(userId));
    }

    @PostMapping("/{dressId}")
    public ResponseEntity<?> toggleFavorite(
            @PathVariable Long dressId,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        Long userId = resolveUserId(authorization);
        AiDressFavoriteService.FavoriteToggleResult result = aiDressFavoriteService.toggleFavorite(userId, dressId);
        return ResponseEntity.ok(Map.of(
                "liked", result.liked(),
                "dressId", result.dressId()
        ));
    }

    @DeleteMapping("/{dressId}")
    public ResponseEntity<?> removeFavorite(
            @PathVariable Long dressId,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        Long userId = resolveUserId(authorization);
        aiDressFavoriteService.removeFavorite(userId, dressId);
        return ResponseEntity.ok(Map.of("liked", false, "dressId", dressId));
    }

    private Long resolveUserId(String authorization) {
        if (authorization == null || authorization.isBlank()) {
            throw new FavoriteAuthException();
        }

        String token = authorization.trim();
        if (token.startsWith("Bearer ")) {
            token = token.substring(7).trim();
        }

        if (!token.startsWith("authenticated-")) {
            throw new FavoriteAuthException();
        }

        try {
            return Long.parseLong(token.substring("authenticated-".length()));
        } catch (NumberFormatException ex) {
            throw new FavoriteAuthException();
        }
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    private static class FavoriteAuthException extends RuntimeException {
    }
}
