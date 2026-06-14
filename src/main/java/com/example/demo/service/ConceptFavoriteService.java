package com.example.demo.service;

import com.example.demo.model.ConceptFavorite;
import com.example.demo.model.ProductItem;
import com.example.demo.model.User;
import com.example.demo.repository.ConceptFavoriteRepository;
import com.example.demo.repository.ProductItemRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConceptFavoriteService {

    private final ConceptFavoriteRepository conceptFavoriteRepository;
    private final ProductItemRepository productItemRepository;
    private final UserRepository userRepository;

    public ConceptFavoriteService(
            ConceptFavoriteRepository conceptFavoriteRepository,
            ProductItemRepository productItemRepository,
            UserRepository userRepository
    ) {
        this.conceptFavoriteRepository = conceptFavoriteRepository;
        this.productItemRepository = productItemRepository;
        this.userRepository = userRepository;
    }

    public List<Long> getFavoriteConceptIdsForUser(Long userId) {
        return conceptFavoriteRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(ConceptFavorite::getProductItemId)
                .toList();
    }

    public FavoriteToggleResult toggleFavorite(Long userId, Long productItemId) {
        validateUser(userId);
        ProductItem concept = validateConcept(productItemId);

        return conceptFavoriteRepository.findByUserIdAndProductItemId(userId, productItemId)
                .map(existing -> {
                    conceptFavoriteRepository.delete(existing);
                    conceptFavoriteRepository.flush();
                    return new FavoriteToggleResult(false, concept.getId(), countFavorites(productItemId));
                })
                .orElseGet(() -> {
                    ConceptFavorite favorite = new ConceptFavorite();
                    favorite.setUserId(userId);
                    favorite.setProductItemId(productItemId);
                    conceptFavoriteRepository.saveAndFlush(favorite);
                    return new FavoriteToggleResult(true, concept.getId(), countFavorites(productItemId));
                });
    }

    public void removeFavorite(Long userId, Long productItemId) {
        validateUser(userId);
        validateConcept(productItemId);
        conceptFavoriteRepository.findByUserIdAndProductItemId(userId, productItemId)
                .ifPresent(conceptFavoriteRepository::delete);
    }

    public boolean isFavorite(Long userId, Long productItemId) {
        return conceptFavoriteRepository.existsByUserIdAndProductItemId(userId, productItemId);
    }

    private ProductItem validateConcept(Long productItemId) {
        ProductItem concept = productItemRepository.findById(productItemId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy concept"));

        if (concept.getCategoryKey() == null || !concept.getCategoryKey().startsWith("concept-noi-bat")) {
            throw new RuntimeException("Sản phẩm này không thuộc nhóm concept");
        }

        return concept;
    }

    private User validateUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
    }

    private long countFavorites(Long productItemId) {
        return conceptFavoriteRepository.countByProductItemId(productItemId);
    }

    public record FavoriteToggleResult(boolean liked, Long productItemId, long favoriteCount) {
    }
}
