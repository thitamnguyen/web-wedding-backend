package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.model.Booking;
import com.example.demo.model.MakeupArtist;
import com.example.demo.model.Profile;
import com.example.demo.model.ProductItem;
import com.example.demo.model.ProductGalleryImage;
import com.example.demo.model.User;
import com.example.demo.repository.BookingRepository;
import com.example.demo.repository.MakeupArtistRepository;
import com.example.demo.repository.ProfileRepository;
import com.example.demo.repository.ProductGalleryImageRepository;
import com.example.demo.repository.ProductItemRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.ProductReviewRepository;
import com.example.demo.service.CloudinaryService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class StaffService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final ProductItemRepository productItemRepository;
    private final ProductReviewRepository productReviewRepository;
    private final ProductGalleryImageRepository productGalleryImageRepository;
    private final ProfileRepository profileRepository;
    private final MakeupArtistRepository makeupArtistRepository;
    private final CloudinaryService cloudinaryService;
    private final PasswordEncoder passwordEncoder;

    public StaffService(
            UserRepository userRepository,
            BookingRepository bookingRepository,
            ProductItemRepository productItemRepository,
            ProductReviewRepository productReviewRepository,
            ProductGalleryImageRepository productGalleryImageRepository,
            ProfileRepository profileRepository,
            MakeupArtistRepository makeupArtistRepository,
            CloudinaryService cloudinaryService,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.productItemRepository = productItemRepository;
        this.productReviewRepository = productReviewRepository;
        this.productGalleryImageRepository = productGalleryImageRepository;
        this.profileRepository = profileRepository;
        this.makeupArtistRepository = makeupArtistRepository;
        this.cloudinaryService = cloudinaryService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public ProductItem createWork(String authHeader, StaffWorkCreateRequest request) {
        StaffContext context = resolveContext(authHeader);
        if (request == null) {
            throw new RuntimeException("Thiếu dữ liệu sản phẩm");
        }
        if (request.getCoverImageFile() == null || request.getCoverImageFile().isEmpty()) {
            throw new RuntimeException("Vui lòng chọn ảnh bìa");
        }

        ProductItem item = new ProductItem();
        item.setTitle(request.getTitle());
        item.setExcerpt(request.getExcerpt());
        item.setContent(request.getContent());
        item.setBadge(request.getBadge());
        item.setSlug((request.getSlug() != null && !request.getSlug().isBlank()) ? request.getSlug() : "staff-" + System.currentTimeMillis());
        item.setPublished(Boolean.TRUE);
        item.setPublishedAt(java.time.LocalDateTime.now());

        if (context.isPhotographer()) {
            if (request.getBookingId() == null) {
                throw new RuntimeException("Vui lòng chọn booking cần gắn album");
            }
            Booking booking = bookingRepository.findById(request.getBookingId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy booking"));
            Long photographerId = resolvePhotographerProfileUserId(context);
            if (!photographerId.equals(booking.getPhotographerId())) {
                throw new RuntimeException("Booking này không thuộc photographer hiện tại");
            }
            if (!"CONFIRMED".equalsIgnoreCase(booking.getStatus())) {
                throw new RuntimeException("Chỉ có thể hoàn thành booking đang CONFIRMED");
            }
            applyPhotographerCategory(item, request.getCategoryKey());
            item.setPhotographerId(photographerId);
            item.setBookingId(booking.getId());
            if (booking.getTotalPrice() == null || booking.getTotalPrice() <= 0) {
                throw new RuntimeException("Booking chua co gia hop le de tao san pham");
            }
            item.setPriceRange(formatPriceRange(booking.getTotalPrice()));
        } else if (context.isMakeup()) {
            item.setCategoryKey("bridal-makeup");
            item.setCategoryLabel("Bridal Makeup");
            item.setMakeupArtistId(context.staffRefId());
            if (request.getBookingId() != null) {
                Booking makeupBooking = bookingRepository.findById(request.getBookingId())
                        .orElseThrow(() -> new RuntimeException("Khong tim thay booking"));
                if (makeupBooking.getTotalPrice() != null && makeupBooking.getTotalPrice() > 0) {
                    item.setBookingId(makeupBooking.getId());
                    item.setPriceRange(formatPriceRange(makeupBooking.getTotalPrice()));
                }
            }
        } else {
            throw new RuntimeException("Nhân viên không hợp lệ");
        }

        if (item.getPriceRange() == null || item.getPriceRange().isBlank()) {
            throw new RuntimeException("Khong the lay gia tu booking");
        }

        if (item.getCategoryLabel() == null || item.getCategoryLabel().isBlank()) {
            setCategoryLabel(item);
        }

        var coverUpload = cloudinaryService.uploadImage(request.getCoverImageFile());
        item.setCoverImageUrl(coverUpload.getSecureUrl());
        item.setPublicId(coverUpload.getPublicId());

        ProductItem saved = productItemRepository.save(item);
        saveGallery(saved, request.getGalleryFiles());

        if (context.isPhotographer()) {
            Booking booking = bookingRepository.findById(request.getBookingId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy booking"));
            booking.setStatus("DONE");
            booking.setPaymentStatus("PAID");
            booking.setRemainingAmount(0.0);
            bookingRepository.save(booking);
        }
        return saved;
    }

    public StaffDashboardResponse getDashboard(String authHeader) {
        StaffContext context = resolveContext(authHeader);
        List<Booking> bookings = loadBookings(context);
        List<ProductItem> works = loadWorks(context);

        StaffProfileDto profile = buildProfile(context);
        StaffSummaryDto summary = buildSummary(bookings, works);
        List<StaffRevenuePointDto> revenue = buildRevenuePoints(bookings);

        return new StaffDashboardResponse(
                toUserDto(context.user),
                profile,
                summary,
                bookings.stream().map(this::toScheduleDto).toList(),
                getWorkBookings(context).stream().map(this::toScheduleDto).toList(),
                works.stream().map(this::toWorkDto).toList(),
                revenue
        );
    }

    public StaffProfileDto getProfile(String authHeader) {
        return buildProfile(resolveContext(authHeader));
    }

    @Transactional
    public StaffProfileDto updateProfile(String authHeader, StaffProfileUpdateRequest request) {
        StaffContext context = resolveContext(authHeader);
        User user = context.user;

        if (request.getFullName() != null && !request.getFullName().trim().isEmpty()) {
            user.setFullName(request.getFullName().trim());
        }
        if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
            user.setPhone(request.getPhone().trim());
        }

        if (context.isPhotographer()) {
            Profile profile = context.profile;
            if (profile == null) {
                profile = new Profile();
                profile.setUserId(user.getStaffRefId() != null ? user.getStaffRefId() : user.getId());
            }
            if (request.getFullName() != null && !request.getFullName().trim().isEmpty()) {
                profile.setFullName(request.getFullName().trim());
            }
            if (request.getJobTitle() != null && !request.getJobTitle().trim().isEmpty()) {
                profile.setJobTitle(request.getJobTitle().trim());
            }
            if (request.getAvatarUrl() != null && !request.getAvatarUrl().trim().isEmpty()) {
                profile.setAvatarUrl(request.getAvatarUrl().trim());
            }
            if (request.getStyle() != null && !request.getStyle().trim().isEmpty()) {
                profile.setStyle(request.getStyle().trim());
            }
            profileRepository.save(profile);
        } else if (context.isMakeup()) {
            MakeupArtist artist = context.makeupArtist;
            if (artist == null) {
                artist = new MakeupArtist();
                artist.setUserId(user.getStaffRefId() != null ? user.getStaffRefId() : user.getId());
            }
            if (request.getFullName() != null && !request.getFullName().trim().isEmpty()) {
                artist.setFullName(request.getFullName().trim());
            }
            if (request.getJobTitle() != null && !request.getJobTitle().trim().isEmpty()) {
                artist.setJobTitle(request.getJobTitle().trim());
            }
            if (request.getAvatarUrl() != null && !request.getAvatarUrl().trim().isEmpty()) {
                artist.setAvatarUrl(request.getAvatarUrl().trim());
            }
            if (request.getSpecialty() != null && !request.getSpecialty().trim().isEmpty()) {
                artist.setSpecialty(request.getSpecialty().trim());
            }
            if (request.getDescription() != null && !request.getDescription().trim().isEmpty()) {
                artist.setDescription(request.getDescription().trim());
            }
            makeupArtistRepository.save(artist);
        } else {
            throw new RuntimeException("Tài khoản nhân viên chưa được gắn hồ sơ làm việc");
        }

        User savedUser = userRepository.save(user);
        StaffContext refreshed = resolveContextFromUser(savedUser);
        return buildProfile(refreshed);
    }

    public List<StaffScheduleDto> getSchedule(String authHeader) {
        StaffContext context = resolveContext(authHeader);
        return loadBookings(context).stream().map(this::toScheduleDto).toList();
    }

    public List<StaffWorkDto> getWorks(String authHeader) {
        StaffContext context = resolveContext(authHeader);
        return loadWorks(context).stream().map(this::toWorkDto).toList();
    }

    public List<StaffScheduleDto> getWorkBookings(String authHeader) {
        StaffContext context = resolveContext(authHeader);
        return getWorkBookings(context).stream().map(this::toScheduleDto).toList();
    }

    public List<StaffRevenuePointDto> getRevenue(String authHeader) {
        return buildRevenuePoints(loadBookings(resolveContext(authHeader)));
    }

    private StaffSummaryDto buildSummary(List<Booking> bookings, List<ProductItem> works) {
        LocalDate today = LocalDate.now();
        long totalBookings = bookings.size();
        long upcomingBookings = bookings.stream()
                .filter(booking -> booking.getBookingDate() != null)
                .filter(booking -> !booking.getBookingDate().isBefore(today))
                .filter(booking -> !"CANCELLED".equalsIgnoreCase(booking.getStatus()))
                .count();
        long completedBookings = bookings.stream()
                .filter(booking -> "DONE".equalsIgnoreCase(booking.getStatus()) || "COMPLETED".equalsIgnoreCase(booking.getStatus()))
                .count();
        long pendingBookings = bookings.stream()
                .filter(booking -> "PENDING".equalsIgnoreCase(booking.getStatus()))
                .count();
        double totalRevenue = bookings.stream()
                .filter(booking -> booking.getTotalPrice() != null)
                .filter(booking -> "CONFIRMED".equalsIgnoreCase(booking.getStatus()) || "DONE".equalsIgnoreCase(booking.getStatus()))
                .mapToDouble(Booking::getTotalPrice)
                .sum();
        double monthRevenue = bookings.stream()
                .filter(booking -> booking.getBookingDate() != null)
                .filter(booking -> YearMonth.from(booking.getBookingDate()).equals(YearMonth.now()))
                .filter(booking -> booking.getTotalPrice() != null)
                .filter(booking -> "CONFIRMED".equalsIgnoreCase(booking.getStatus()) || "DONE".equalsIgnoreCase(booking.getStatus()))
                .mapToDouble(Booking::getTotalPrice)
                .sum();

        return new StaffSummaryDto(
                totalBookings,
                upcomingBookings,
                completedBookings,
                pendingBookings,
                works.size(),
                totalRevenue,
                monthRevenue
        );
    }

    private StaffProfileDto buildProfile(StaffContext context) {
        if (context.isPhotographer()) {
            Profile profile = context.profile;
            return new StaffProfileDto(
                    context.user.getId(),
                    profile != null && profile.getFullName() != null ? profile.getFullName() : context.user.getFullName(),
                    profile != null ? profile.getJobTitle() : "Wedding Photographer",
                    profile != null ? profile.getAvatarUrl() : null,
                    profile != null ? profile.getStyle() : null,
                    null,
                    null,
                    profile != null ? profile.getExperienceYears() : null,
                    profile != null && profile.getRating() != null ? profile.getRating().doubleValue() : null,
                    profile != null ? profile.getReviewCount() : null,
                    profile != null ? profile.getFeaturedWork() : null,
                    profile != null && profile.getTotalRevenue() != null ? profile.getTotalRevenue() : BigDecimal.ZERO,
                    "PHOTOGRAPHER"
            );
        }

        if (context.isMakeup()) {
            MakeupArtist artist = context.makeupArtist;
            return new StaffProfileDto(
                    context.user.getId(),
                    artist != null && artist.getFullName() != null ? artist.getFullName() : context.user.getFullName(),
                    artist != null ? artist.getJobTitle() : "Makeup Artist",
                    artist != null ? artist.getAvatarUrl() : null,
                    null,
                    artist != null ? artist.getSpecialty() : null,
                    artist != null ? artist.getDescription() : null,
                    null,
                    null,
                    null,
                    null,
                    artist != null && artist.getTotalRevenue() != null ? artist.getTotalRevenue() : BigDecimal.ZERO,
                    "MAKEUP"
            );
        }

        return new StaffProfileDto(
                context.user.getId(),
                context.user.getFullName(),
                "Nhân viên",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                BigDecimal.ZERO,
                "STAFF"
        );
    }

    private List<Booking> loadBookings(StaffContext context) {
        Long refId = null;
        
        if (context.isPhotographer()) {
            // For photographers: use userId from profile
            refId = resolvePhotographerProfileUserId(context);
        } else {
            // For makeup artists: use makeup artist ID directly
            if (context.makeupArtist() != null && context.makeupArtist().getId() != null) {
                refId = context.makeupArtist().getId();
            } else if (context.staffRefId() != null) {
                // Fallback to staffRefId if set
                refId = context.staffRefId();
            }
        }
        
        if (refId == null) {
            return List.of();
        }

        List<Booking> bookings = context.isPhotographer()
                ? bookingRepository.findByPhotographerIdOrderByBookingDateDesc(refId)
                : bookingRepository.findByMakeupArtistIdOrderByBookingDateDesc(refId);

        return bookings.stream()
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

    private List<ProductItem> loadWorks(StaffContext context) {
        Long refId = null;
        
        if (context.isPhotographer()) {
            // For photographers: use userId from profile
            refId = resolvePhotographerProfileUserId(context);
        } else {
            // For makeup artists: use makeup artist ID directly
            if (context.makeupArtist() != null && context.makeupArtist().getId() != null) {
                refId = context.makeupArtist().getId();
            } else if (context.staffRefId() != null) {
                // Fallback to staffRefId if set
                refId = context.staffRefId();
            }
        }
        
        if (refId == null) {
            return List.of();
        }

        return context.isPhotographer()
                ? productItemRepository.findByPhotographerIdAndPublishedTrueOrderByPublishedAtDesc(refId)
                : productItemRepository.findByMakeupArtistIdAndPublishedTrueOrderByPublishedAtDesc(refId);
    }

    private List<Booking> getWorkBookings(StaffContext context) {
        Long refId = resolvePhotographerProfileUserId(context);
        if (refId == null) {
            return List.of();
        }

        if (!context.isPhotographer()) {
            return List.of();
        }

        LocalDate today = LocalDate.now();

        return bookingRepository.findByPhotographerIdAndStatus(refId, "CONFIRMED").stream()
                .filter(booking -> booking.getBookingDate() != null)
                .filter(booking -> !booking.getBookingDate().isAfter(today))
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

    private List<StaffRevenuePointDto> buildRevenuePoints(List<Booking> bookings) {
        Map<String, Double> grouped = new LinkedHashMap<>();
        List<YearMonth> months = new ArrayList<>();
        YearMonth current = YearMonth.now();
        for (int i = 5; i >= 0; i--) {
            months.add(current.minusMonths(i));
        }

        for (YearMonth month : months) {
            double amount = bookings.stream()
                    .filter(booking -> booking.getBookingDate() != null && YearMonth.from(booking.getBookingDate()).equals(month))
                    .filter(booking -> booking.getTotalPrice() != null)
                    .filter(booking -> "CONFIRMED".equalsIgnoreCase(booking.getStatus()) || "DONE".equalsIgnoreCase(booking.getStatus()))
                    .mapToDouble(Booking::getTotalPrice)
                    .sum();
            grouped.put(month.toString(), amount);
        }

        return grouped.entrySet().stream()
                .map(entry -> new StaffRevenuePointDto(entry.getKey(), entry.getValue()))
                .toList();
    }

    private StaffScheduleDto toScheduleDto(Booking booking) {
        double totalPrice = booking.getTotalPrice() != null ? booking.getTotalPrice() : 0.0;
        double deposit = Math.round(totalPrice * 0.2d);
        String serviceTitle = booking.getServicePackage() != null ? booking.getServicePackage().getName() : "Gói dịch vụ cưới";

        return new StaffScheduleDto(
                booking.getId(),
                booking.getCustomerName(),
                booking.getBookingDate() != null ? booking.getBookingDate().format(DATE_FORMAT) : null,
                serviceTitle,
                booking.getStatus(),
                booking.getPaymentStatus(),
                totalPrice,
                deposit,
                null,
                booking.getMessage(),
                booking.getUserId()
        );
    }

    private StaffWorkDto toWorkDto(ProductItem item) {
        return new StaffWorkDto(
                item.getId(),
                item.getTitle(),
                item.getSlug(),
                item.getCategoryLabel(),
                item.getExcerpt(),
                item.getCoverImageUrl(),
                item.getPriceRange(),
                item.getBadge(),
                item.getPublishedAt(),
                productReviewRepository.averageRating(item.getId()),
                productReviewRepository.countByProductItemId(item.getId())
        );
    }

    private void applyPhotographerCategory(ProductItem item, String categoryKey) {
        String normalized = (categoryKey == null || categoryKey.isBlank()) ? "concept-noi-bat" : categoryKey;
        switch (normalized) {
            case "concept-noi-bat" -> item.setCategoryLabel("Concept Nổi Bật");
            case "album-phong-su-cuoi" -> item.setCategoryLabel("Album Phóng Sự Cưới");
            case "album-pre-wedding" -> item.setCategoryLabel("Album Pre-Wedding");
            default -> throw new RuntimeException("Phân loại không hợp lệ cho Photographer");
        }
        item.setCategoryKey(normalized);
    }

    private void setCategoryLabel(ProductItem item) {
        if (item.getCategoryKey() == null) {
            return;
        }
        switch (item.getCategoryKey()) {
            case "concept-noi-bat" -> item.setCategoryLabel("Concept Nổi Bật");
            case "album-pre-wedding" -> item.setCategoryLabel("Album Pre-Wedding");
            case "bst-vay-cuoi" -> item.setCategoryLabel("BST Váy Cưới");
            case "album-phong-su-cuoi" -> item.setCategoryLabel("Album Phóng Sự Cưới");
            case "bridal-makeup" -> item.setCategoryLabel("Bridal Makeup");
            default -> {
            }
        }
    }

    private void saveGallery(ProductItem productItem, MultipartFile[] files) {
        if (files == null) {
            return;
        }
        int index = 0;
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }
            var upload = cloudinaryService.uploadImage(file);
            ProductGalleryImage image = new ProductGalleryImage();
            image.setProductItem(productItem);
            image.setImageUrl(upload.getSecureUrl());
            image.setPublicId(upload.getPublicId());
            image.setSortOrder(index++);
            productGalleryImageRepository.save(image);
        }
    }

    private String formatPriceRange(Double totalPrice) {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);
        numberFormat.setGroupingUsed(true);
        numberFormat.setMinimumFractionDigits(0);
        numberFormat.setMaximumFractionDigits(0);
        return numberFormat.format(Math.round(totalPrice));
    }

    private ProfileUserDto toUserDto(User user) {
        return new ProfileUserDto(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole() != null ? user.getRole().getName() : "ROLE_STAFF",
                buildInitials(user.getFullName(), user.getEmail())
        );
    }

    private String buildInitials(String fullName, String email) {
        String source = (fullName != null && !fullName.isBlank()) ? fullName : email;
        if (source == null || source.isBlank()) {
            return "S";
        }
        String initials = java.util.Arrays.stream(source.trim().split("\\s+"))
                .filter(part -> !part.isBlank())
                .map(part -> part.substring(0, 1))
                .limit(2)
                .collect(Collectors.joining());
        return initials.isBlank() ? "S" : initials.toUpperCase(Locale.ROOT);
    }

    private StaffContext resolveContext(String authHeader) {
        Long userId = extractUserId(authHeader);
        if (userId == null) {
            throw new RuntimeException("Chưa đăng nhập");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        return resolveContextFromUser(user);
    }

    private StaffContext resolveContextFromUser(User user) {
        String roleName = user.getRole() != null ? user.getRole().getName() : "";
        if (!"ROLE_STAFF".equalsIgnoreCase(roleName)) {
            throw new RuntimeException("Tài khoản không có quyền nhân viên");
        }

        String staffType = user.getStaffType();
        Long refId = user.getStaffRefId();

        Profile profile = null;
        MakeupArtist makeupArtist = null;

        if ("PHOTOGRAPHER".equalsIgnoreCase(staffType) || (staffType == null && refId != null && profileRepository.findById(refId).isPresent())) {
            Long profileId = refId != null ? refId : user.getId();
            profile = profileRepository.findById(profileId).orElse(null);
            return new StaffContext(user, "PHOTOGRAPHER", profileId, profile, null);
        }

        if ("MAKEUP".equalsIgnoreCase(staffType) || (staffType == null && refId != null && makeupArtistRepository.findById(refId).isPresent())) {
            Long makeupId = refId != null ? refId : user.getId();
            makeupArtist = makeupArtistRepository.findById(makeupId).orElse(null);
            return new StaffContext(user, "MAKEUP", makeupId, null, makeupArtist);
        }

        Long profileId = profileRepository.findById(user.getId()).map(Profile::getUserId).orElse(null);
        if (profileId != null) {
            profile = profileRepository.findById(profileId).orElse(null);
            return new StaffContext(user, "PHOTOGRAPHER", profileId, profile, null);
        }

        Long makeupId = makeupArtistRepository.findAll().stream()
                .filter(artist -> Objects.equals(artist.getUserId(), user.getId()))
                .map(MakeupArtist::getId)
                .findFirst()
                .orElse(null);
        if (makeupId != null) {
            makeupArtist = makeupArtistRepository.findById(makeupId).orElse(null);
            return new StaffContext(user, "MAKEUP", makeupId, null, makeupArtist);
        }

        throw new RuntimeException("Tài khoản nhân viên chưa được gắn hồ sơ làm việc");
    }

    private Long resolvePhotographerProfileUserId(StaffContext context) {
        if (context == null) {
            return null;
        }
        // Always prioritize profile.userId since photographer_id in Booking is stored as userId
        if (context.profile() != null && context.profile().getUserId() != null) {
            return context.profile().getUserId();
        }
        // Fallback to user.id only (not staffRefId which may be different)
        return context.user() != null ? context.user().getId() : null;
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

    private record StaffContext(
            User user,
            String staffType,
            Long staffRefId,
            Profile profile,
            MakeupArtist makeupArtist
    ) {
        boolean isPhotographer() {
            return "PHOTOGRAPHER".equalsIgnoreCase(staffType);
        }

        boolean isMakeup() {
            return "MAKEUP".equalsIgnoreCase(staffType);
        }
    }
}
