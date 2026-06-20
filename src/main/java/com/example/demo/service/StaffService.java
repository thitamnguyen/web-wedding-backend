package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.model.Booking;
import com.example.demo.model.MakeupArtist;
import com.example.demo.model.Profile;
import com.example.demo.model.ProductItem;
import com.example.demo.model.User;
import com.example.demo.repository.BookingRepository;
import com.example.demo.repository.MakeupArtistRepository;
import com.example.demo.repository.ProfileRepository;
import com.example.demo.repository.ProductItemRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
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
    private final ProfileRepository profileRepository;
    private final MakeupArtistRepository makeupArtistRepository;
    private final PasswordEncoder passwordEncoder;

    public StaffService(
            UserRepository userRepository,
            BookingRepository bookingRepository,
            ProductItemRepository productItemRepository,
            ProfileRepository profileRepository,
            MakeupArtistRepository makeupArtistRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.productItemRepository = productItemRepository;
        this.profileRepository = profileRepository;
        this.makeupArtistRepository = makeupArtistRepository;
        this.passwordEncoder = passwordEncoder;
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
                "STAFF"
        );
    }

    private List<Booking> loadBookings(StaffContext context) {
        Long refId = context.staffRefId();
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
        Long refId = context.staffRefId();
        if (refId == null) {
            return List.of();
        }

        return context.isPhotographer()
                ? productItemRepository.findByPhotographerIdAndPublishedTrueOrderByPublishedAtDesc(refId)
                : productItemRepository.findByMakeupArtistIdAndPublishedTrueOrderByPublishedAtDesc(refId);
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
        String serviceTitle = booking.getWeddingService() != null ? booking.getWeddingService().getTitle() : "Gói dịch vụ cưới";

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
                item.getPublishedAt()
        );
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
