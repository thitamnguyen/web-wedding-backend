package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.model.Booking;
import com.example.demo.model.AiDressFavorite;
import com.example.demo.model.ConceptFavorite;
import com.example.demo.model.ProductItem;
import com.example.demo.model.Review;
import com.example.demo.model.WeddingDress;
import com.example.demo.model.User;
import com.example.demo.repository.AiDressFavoriteRepository;
import com.example.demo.repository.BookingRepository;
import com.example.demo.repository.ConceptFavoriteRepository;
import com.example.demo.repository.ProductItemRepository;
import com.example.demo.repository.ReviewRepository;
import com.example.demo.repository.WeddingDressRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ProfileService {

    private static final double DEPOSIT_RATE = 0.2d;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final ReviewRepository reviewRepository;
    private final ConceptFavoriteRepository conceptFavoriteRepository;
    private final AiDressFavoriteRepository aiDressFavoriteRepository;
    private final ProductItemRepository productItemRepository;
    private final WeddingDressRepository weddingDressRepository;
    private final PasswordEncoder passwordEncoder;

    public ProfileService(
            UserRepository userRepository,
            BookingRepository bookingRepository,
            ReviewRepository reviewRepository,
            ConceptFavoriteRepository conceptFavoriteRepository,
            AiDressFavoriteRepository aiDressFavoriteRepository,
            ProductItemRepository productItemRepository,
            WeddingDressRepository weddingDressRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.reviewRepository = reviewRepository;
        this.conceptFavoriteRepository = conceptFavoriteRepository;
        this.aiDressFavoriteRepository = aiDressFavoriteRepository;
        this.productItemRepository = productItemRepository;
        this.weddingDressRepository = weddingDressRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public ProfileDashboardResponse getDashboard(String authHeader) {
        User user = resolveUser(authHeader);
        List<Booking> bookings = loadBookings(user);
        Map<Long, Booking> bookingMap = bookings.stream()
                .filter(b -> b.getId() != null)
                .collect(Collectors.toMap(Booking::getId, b -> b, (a, b) -> a, LinkedHashMap::new));

        List<Review> reviews = bookingMap.isEmpty()
                ? List.of()
                : reviewRepository.findByBookingIdInOrderByCreatedAtDesc(new ArrayList<>(bookingMap.keySet()));
        Map<Long, Review> reviewMap = reviews.stream()
                .filter(r -> r.getBookingId() != null)
                .collect(Collectors.toMap(Review::getBookingId, r -> r, (a, b) -> a, LinkedHashMap::new));

        List<ConceptFavorite> favorites = conceptFavoriteRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        Map<Long, ProductItem> productMap = productItemRepository.findAllById(
                        favorites.stream().map(ConceptFavorite::getProductItemId).toList())
                .stream()
                .collect(Collectors.toMap(ProductItem::getId, item -> item, (a, b) -> a, LinkedHashMap::new));

        List<ProfileFavoriteConceptDto> favoriteDtos = favorites.stream()
                .map(favorite -> toFavoriteDto(favorite, productMap.get(favorite.getProductItemId())))
                .filter(Objects::nonNull)
                .toList();

        List<AiDressFavorite> aiFavorites = aiDressFavoriteRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        Map<Long, WeddingDress> dressMap = weddingDressRepository.findAllById(
                        aiFavorites.stream().map(AiDressFavorite::getDressId).toList())
                .stream()
                .collect(Collectors.toMap(WeddingDress::getId, dress -> dress, (a, b) -> a, LinkedHashMap::new));

        List<ProfileAiDressFavoriteDto> aiFavoriteDtos = aiFavorites.stream()
                .map(favorite -> toAiDressFavoriteDto(favorite, dressMap.get(favorite.getDressId())))
                .filter(Objects::nonNull)
                .toList();

        List<ProfileBookingDto> bookingDtos = bookings.stream()
                .map(booking -> toBookingDto(booking, reviewMap.get(booking.getId())))
                .toList();

        List<ProfileAlbumDto> albumDtos = bookings.stream()
                .filter(booking -> "DONE".equalsIgnoreCase(booking.getStatus()))
                .map(this::toAlbumDto)
                .filter(Objects::nonNull)
                .toList();

        List<ProfilePaymentDto> paymentDtos = bookings.stream()
                .map(this::toPaymentDto)
                .toList();

        List<ProfileReviewDto> reviewDtos = bookings.stream()
                .map(booking -> toReviewDto(booking, reviewMap.get(booking.getId())))
                .filter(Objects::nonNull)
                .toList();

        ProfileSummaryDto summary = buildSummary(bookings, favoriteDtos, reviewDtos);

        return new ProfileDashboardResponse(
                toUserDto(user),
                summary,
                bookingDtos,
                favoriteDtos,
                aiFavoriteDtos,
                albumDtos,
                paymentDtos,
                reviewDtos
        );
    }

    public ProfileUserDto updateProfile(String authHeader, ProfileUpdateRequest request) {
        User user = resolveUser(authHeader);
        if (request.getFullName() != null && !request.getFullName().trim().isEmpty()) {
            user.setFullName(request.getFullName().trim());
        }
        if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
            user.setPhone(request.getPhone().trim());
        }
        User saved = userRepository.save(user);
        return toUserDto(saved);
    }

    public ProfileUserDto changePassword(String authHeader, ChangePasswordRequest request) {
        User user = resolveUser(authHeader);
        if (request.getOldPassword() == null || request.getOldPassword().isBlank()) {
            throw new RuntimeException("Vui lòng nhập mật khẩu cũ");
        }
        if (request.getNewPassword() == null || request.getNewPassword().length() < 6) {
            throw new RuntimeException("Mật khẩu mới phải có ít nhất 6 ký tự");
        }
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Mật khẩu cũ không đúng");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        User saved = userRepository.save(user);
        return toUserDto(saved);
    }

    private ProfileSummaryDto buildSummary(List<Booking> bookings, List<ProfileFavoriteConceptDto> favorites, List<ProfileReviewDto> reviews) {
        LocalDate today = LocalDate.now();
        long totalBookings = bookings.size();
        long upcomingBookings = bookings.stream()
                .filter(booking -> booking.getBookingDate() != null)
                .filter(booking -> !booking.getBookingDate().isBefore(today))
                .filter(booking -> !"CANCELLED".equalsIgnoreCase(booking.getStatus()))
                .count();
        long completedBookings = bookings.stream()
                .filter(booking -> "DONE".equalsIgnoreCase(booking.getStatus()))
                .count();
        long unpaidBookings = bookings.stream()
                .filter(booking -> booking.getPaymentStatus() == null || !"PAID".equalsIgnoreCase(booking.getPaymentStatus()))
                .filter(booking -> !"CANCELLED".equalsIgnoreCase(booking.getStatus()))
                .count();
        double totalSpent = bookings.stream()
                .filter(booking -> "DONE".equalsIgnoreCase(booking.getStatus()) || "CONFIRMED".equalsIgnoreCase(booking.getStatus()))
                .map(Booking::getTotalPrice)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .sum();

        return new ProfileSummaryDto(
                totalBookings,
                upcomingBookings,
                completedBookings,
                favorites.size(),
                reviews.size(),
                unpaidBookings,
                totalSpent
        );
    }

    private List<Booking> loadBookings(User user) {
        List<Booking> byPhone = user.getPhone() == null || user.getPhone().isBlank()
                ? List.of()
                : bookingRepository.findByCustomerPhoneOrderByBookingDateDesc(user.getPhone());
        List<Booking> byEmail = user.getEmail() == null || user.getEmail().isBlank()
                ? List.of()
                : bookingRepository.findByCustomerEmailOrderByBookingDateDesc(user.getEmail());

        LinkedHashMap<Long, Booking> merged = new LinkedHashMap<>();
        for (Booking booking : byPhone) {
            if (booking.getId() != null) {
                merged.put(booking.getId(), booking);
            }
        }
        for (Booking booking : byEmail) {
            if (booking.getId() != null) {
                merged.putIfAbsent(booking.getId(), booking);
            }
        }
        return merged.values().stream()
                .sorted((left, right) -> {
                    LocalDate leftDate = left.getBookingDate();
                    LocalDate rightDate = right.getBookingDate();
                    if (leftDate == null && rightDate == null) {
                        return Long.compare(right.getId() == null ? 0 : right.getId(), left.getId() == null ? 0 : left.getId());
                    }
                    if (leftDate == null) return 1;
                    if (rightDate == null) return -1;
                    int dateCompare = rightDate.compareTo(leftDate);
                    if (dateCompare != 0) {
                        return dateCompare;
                    }
                    return Long.compare(right.getId() == null ? 0 : right.getId(), left.getId() == null ? 0 : left.getId());
                })
                .toList();
    }

    private ProfileUserDto toUserDto(User user) {
        return new ProfileUserDto(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole() != null ? user.getRole().getName() : "ROLE_CLIENT",
                buildInitials(user.getFullName(), user.getEmail())
        );
    }

    private ProfileBookingDto toBookingDto(Booking booking, Review review) {
        String serviceTitle = booking.getServicePackage() != null ? booking.getServicePackage().getName() : "Gói dịch vụ cưới";
        String serviceImageUrl = (booking.getServicePackage() != null && booking.getServicePackage().getCategory() != null) ? booking.getServicePackage().getCategory().getImageUrl() : null;
        String photographerName = booking.getPhotographerProfile() != null ? booking.getPhotographerProfile().getFullName() : null;
        String makeupArtistName = booking.getMakeupArtist() != null ? booking.getMakeupArtist().getFullName() : null;
        boolean hasReview = review != null;
        String reviewLabel = hasReview ? "Đã đánh giá" : ("DONE".equalsIgnoreCase(booking.getStatus()) ? "Chờ đánh giá" : "Chưa áp dụng");

        return new ProfileBookingDto(
                booking.getId(),
                booking.getCustomerName(),
                formatDate(booking.getBookingDate()),
                serviceTitle,
                serviceImageUrl,
                booking.getStatus(),
                booking.getPaymentStatus(),
                booking.getTotalPrice(),
                calculateDeposit(booking.getTotalPrice()),
                booking.getPhotographerId(),
                photographerName,
                booking.getMakeupArtistId(),
                makeupArtistName,
                booking.getMessage(),
                hasReview,
                reviewLabel
        );
    }

    private ProfileFavoriteConceptDto toFavoriteDto(ConceptFavorite favorite, ProductItem item) {
        if (item == null) {
            return null;
        }
        return new ProfileFavoriteConceptDto(
                favorite.getId(),
                favorite.getProductItemId(),
                item.getTitle(),
                item.getSlug(),
                item.getExcerpt(),
                item.getCoverImageUrl(),
                item.getCategoryLabel(),
                item.getPriceRange(),
                item.getBadge(),
                true,
                conceptFavoriteRepository.countByProductItemId(item.getId())
        );
    }

    private ProfileAiDressFavoriteDto toAiDressFavoriteDto(AiDressFavorite favorite, WeddingDress dress) {
        if (dress == null) {
            return null;
        }

        return new ProfileAiDressFavoriteDto(
                favorite.getId(),
                favorite.getDressId(),
                dress.getDressName(),
                dress.getDressType(),
                dress.getStyle(),
                dress.getBodyShape(),
                dress.getPrice(),
                dress.getImageUrl(),
                dress.getDescription(),
                true
        );
    }

    private ProfileAlbumDto toAlbumDto(Booking booking) {
        ProductItem item = productItemRepository.findByBookingId(booking.getId()).orElse(null);
        if (item == null) {
            return null;
        }

        return new ProfileAlbumDto(
                booking.getId(),
                item.getSlug(),
                item.getTitle(),
                formatDate(booking.getBookingDate()),
                item.getCoverImageUrl(),
                item.getCategoryLabel(),
                booking.getStatus(),
                booking.getPhotographerProfile() != null ? booking.getPhotographerProfile().getFullName() : null,
                booking.getMakeupArtist() != null ? booking.getMakeupArtist().getFullName() : null,
                item.getExcerpt(),
                item.getAverageRating(),
                item.getReviewCount()
        );
    }

    private ProfilePaymentDto toPaymentDto(Booking booking) {
        double totalPrice = booking.getTotalPrice() != null ? booking.getTotalPrice() : 0.0;
        double deposit = calculateDeposit(booking.getTotalPrice());
        double remaining = Math.max(0, totalPrice - deposit);
        String serviceTitle = booking.getServicePackage() != null ? booking.getServicePackage().getName() : "Gói dịch vụ cưới";
        String qrImageUrl = "https://img.vietqr.io/image/MB-0987654321-compact.png?amount="
                + Math.round(deposit)
                + "&addInfo=STUDIOWS"
                + booking.getId()
                + "&accountName=WEDDING%20STUDIO";

        return new ProfilePaymentDto(
                booking.getId(),
                serviceTitle,
                formatDate(booking.getBookingDate()),
                totalPrice,
                deposit,
                remaining,
                booking.getPaymentStatus(),
                booking.getStatus(),
                "STUDIOWS" + booking.getId(),
                qrImageUrl
        );
    }

    private ProfileReviewDto toReviewDto(Booking booking, Review review) {
        if (review == null) {
            return new ProfileReviewDto(
                    null,
                    booking.getId(),
                    booking.getServicePackage() != null ? booking.getServicePackage().getName() : "Gói dịch vụ cưới",
                    null,
                    null,
                    null,
                    booking.getPhotographerProfile() != null ? booking.getPhotographerProfile().getFullName() : null,
                    booking.getMakeupArtist() != null ? booking.getMakeupArtist().getFullName() : null,
                    formatDate(booking.getBookingDate()),
                    "DONE".equalsIgnoreCase(booking.getStatus())
            );
        }

        return new ProfileReviewDto(
                review.getId(),
                booking.getId(),
                booking.getServicePackage() != null ? booking.getServicePackage().getName() : "Gói dịch vụ cưới",
                review.getRating(),
                review.getComment(),
                review.getCreatedAt() != null ? review.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : null,
                booking.getPhotographerProfile() != null ? booking.getPhotographerProfile().getFullName() : null,
                booking.getMakeupArtist() != null ? booking.getMakeupArtist().getFullName() : null,
                formatDate(booking.getBookingDate()),
                false
        );
    }

    private double calculateDeposit(Double totalPrice) {
        if (totalPrice == null) {
            return 0.0;
        }
        return Math.round(totalPrice * DEPOSIT_RATE);
    }

    private String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMAT) : null;
    }

    private String buildInitials(String fullName, String email) {
        String source = (fullName != null && !fullName.isBlank()) ? fullName : email;
        if (source == null || source.isBlank()) {
            return "U";
        }
        String initials = java.util.Arrays.stream(source.trim().split("\\s+"))
                .filter(part -> !part.isBlank())
                .map(part -> part.substring(0, 1))
                .limit(2)
                .collect(Collectors.joining());
        return initials.isBlank() ? "U" : initials.toUpperCase(Locale.ROOT);
    }

    private User resolveUser(String authHeader) {
        Long userId = extractUserId(authHeader);
        if (userId == null) {
            throw new RuntimeException("Chưa đăng nhập");
        }
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
    }

    private Long extractUserId(String authHeader) {
        if (authHeader == null || authHeader.isBlank()) {
            return null;
        }

        String token = authHeader.trim();
        if (token.toLowerCase(Locale.ROOT).startsWith("bearer ")) {
            token = token.substring(7).trim();
        }

        if (!token.startsWith("authenticated-")) {
            return null;
        }

        try {
            return Long.parseLong(token.substring("authenticated-".length()));
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
