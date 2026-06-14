package com.example.demo.controller;

import com.example.demo.service.ConceptFavoriteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/concept-favorites")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class ConceptFavoriteController {

    private final ConceptFavoriteService conceptFavoriteService;

    public ConceptFavoriteController(ConceptFavoriteService conceptFavoriteService) {
        this.conceptFavoriteService = conceptFavoriteService;
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMyFavorites(@RequestHeader(value = "Authorization", required = false) String authorization) {
        Long userId = resolveUserId(authorization);
        return ResponseEntity.ok(conceptFavoriteService.getFavoriteConceptIdsForUser(userId));
    }

    @PostMapping("/{productItemId}")
    public ResponseEntity<?> toggleFavorite(
            @PathVariable Long productItemId,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        Long userId = resolveUserId(authorization);
        ConceptFavoriteService.FavoriteToggleResult result = conceptFavoriteService.toggleFavorite(userId, productItemId);
        return ResponseEntity.ok(Map.of(
                "liked", result.liked(),
                "productItemId", result.productItemId(),
                "favoriteCount", result.favoriteCount()
        ));
    }

    @DeleteMapping("/{productItemId}")
    public ResponseEntity<?> removeFavorite(
            @PathVariable Long productItemId,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        Long userId = resolveUserId(authorization);
        conceptFavoriteService.removeFavorite(userId, productItemId);
        return ResponseEntity.ok(Map.of("liked", false, "productItemId", productItemId));
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
