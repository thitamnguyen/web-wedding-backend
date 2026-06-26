package com.example.demo.service;

import com.example.demo.model.AiDressFavorite;
import com.example.demo.model.WeddingDress;
import com.example.demo.repository.AiDressFavoriteRepository;
import com.example.demo.repository.WeddingDressRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AiDressFavoriteService {

    private final AiDressFavoriteRepository aiDressFavoriteRepository;
    private final WeddingDressRepository weddingDressRepository;

    public AiDressFavoriteService(
            AiDressFavoriteRepository aiDressFavoriteRepository,
            WeddingDressRepository weddingDressRepository
    ) {
        this.aiDressFavoriteRepository = aiDressFavoriteRepository;
        this.weddingDressRepository = weddingDressRepository;
    }

    public List<Long> getFavoriteDressIdsForUser(Long userId) {
        return aiDressFavoriteRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(AiDressFavorite::getDressId)
                .toList();
    }

    public FavoriteToggleResult toggleFavorite(Long userId, Long dressId) {
        WeddingDress dress = validateDress(dressId);
        return aiDressFavoriteRepository.findByUserIdAndDressId(userId, dressId)
                .map(existing -> {
                    aiDressFavoriteRepository.delete(existing);
                    aiDressFavoriteRepository.flush();
                    return new FavoriteToggleResult(false, dress.getId());
                })
                .orElseGet(() -> {
                    AiDressFavorite favorite = new AiDressFavorite();
                    favorite.setUserId(userId);
                    favorite.setDressId(dressId);
                    aiDressFavoriteRepository.saveAndFlush(favorite);
                    return new FavoriteToggleResult(true, dress.getId());
                });
    }

    public void removeFavorite(Long userId, Long dressId) {
        validateDress(dressId);
        aiDressFavoriteRepository.findByUserIdAndDressId(userId, dressId)
                .ifPresent(aiDressFavoriteRepository::delete);
    }

    public boolean isFavorite(Long userId, Long dressId) {
        return aiDressFavoriteRepository.existsByUserIdAndDressId(userId, dressId);
    }

    public List<WeddingDress> getFavoriteDressesForUser(Long userId) {
        List<Long> dressIds = getFavoriteDressIdsForUser(userId);
        return dressIds.isEmpty() ? List.of() : weddingDressRepository.findAllById(dressIds);
    }

    private WeddingDress validateDress(Long dressId) {
        return weddingDressRepository.findById(dressId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy váy cưới"));
    }

    public record FavoriteToggleResult(boolean liked, Long dressId) {
    }
}
