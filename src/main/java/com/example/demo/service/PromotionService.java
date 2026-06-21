package com.example.demo.service;

import com.example.demo.model.Promotion;
import com.example.demo.repository.BookingRepository;
import com.example.demo.repository.PromotionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PromotionService {

    private final PromotionRepository promotionRepository;
    private final BookingRepository bookingRepository;

    public PromotionService(PromotionRepository promotionRepository, BookingRepository bookingRepository) {
        this.promotionRepository = promotionRepository;
        this.bookingRepository = bookingRepository;
    }

    public List<Promotion> getActivePromotions() {
        LocalDate today = LocalDate.now();
        return promotionRepository
                .findByActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByStartDateDescIdDesc(today, today)
                .stream()
                .sorted(Comparator
                        .comparing(Promotion::getStartDate, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(Promotion::getId, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    public List<Promotion> getAllPromotionsNewestFirst() {
        return promotionRepository.findAll().stream()
                .sorted(Comparator
                        .comparing(Promotion::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(Promotion::getStartDate, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(Promotion::getId, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    public Optional<Promotion> findValidPromotionByCode(String rawCode) {
        return findValidPromotionByCode(rawCode, null);
    }

    public Optional<Promotion> findValidPromotionByCode(String rawCode, Long userId) {
        if (rawCode == null || rawCode.isBlank()) {
            return Optional.empty();
        }

        String normalized = normalizeCode(rawCode);
        return findPromotionByCode(rawCode)
                .filter(promotion -> !isPromotionUsedByUser(userId, promotion));
    }

    public List<Promotion> getActivePromotionsForUser(Long userId) {
        Set<String> usedCodes = getUsedPromotionCodes(userId);
        return getAllPromotionsNewestFirst().stream()
                .peek(promotion -> promotion.setUsedByUser(usedCodes.contains(normalizeCode(promotion.getCode()))))
                .toList();
    }

    public Optional<Promotion> findPromotionByCode(String rawCode) {
        if (rawCode == null || rawCode.isBlank()) {
            return Optional.empty();
        }

        String normalized = normalizeCode(rawCode);
        LocalDate today = LocalDate.now();

        return promotionRepository.findAll().stream()
                .filter(Promotion::getActive)
                .filter(p -> isWithinDateRange(p, today))
                .filter(p -> normalized.equals(normalizeCode(p.getCode()))
                        || normalized.equals(normalizeCode(p.getName())))
                .findFirst();
    }

    @Transactional
    @Scheduled(cron = "0 0 2 * * *")
    public void cleanupExpiredPromotions() {
        promotionRepository.deleteExpiredPromotions(LocalDate.now());
    }

    private boolean isWithinDateRange(Promotion promotion, LocalDate date) {
        return promotion != null
                && promotion.getStartDate() != null
                && promotion.getEndDate() != null
                && !date.isBefore(promotion.getStartDate())
                && !date.isAfter(promotion.getEndDate());
    }

    private String normalizeCode(String value) {
        if (value == null) {
            return "";
        }
        String upper = value.trim().toUpperCase(Locale.ROOT);
        return upper.replaceAll("[^A-Z0-9]+", "");
    }

    public boolean isPromotionUsedByUser(Long userId, Promotion promotion) {
        if (userId == null || promotion == null) {
            return false;
        }

        String code = normalizeCode(promotion.getCode());
        if (code.isBlank()) {
            return false;
        }

        return getUsedPromotionCodes(userId).contains(code);
    }

    private Set<String> getUsedPromotionCodes(Long userId) {
        if (userId == null) {
            return Set.of();
        }

        return bookingRepository.findByUserIdAndPromotionCodeIsNotNull(userId).stream()
                .map(booking -> booking.getPromotionCode())
                .filter(code -> code != null && !code.isBlank())
                .map(this::normalizeCode)
                .collect(Collectors.toSet());
    }
}
