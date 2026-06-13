package com.example.demo.service;

import com.example.demo.model.Booking;
import com.example.demo.model.MakeupArtist;
import com.example.demo.model.Profile;
import com.example.demo.model.WeddingService;
import com.example.demo.repository.BookingRepository;
import com.example.demo.repository.MakeupArtistRepository;
import com.example.demo.repository.ProfileRepository;
import com.example.demo.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class BookingService {

    private static final Set<String> ALLOWED_STATUSES = Set.of("PENDING", "APPROVED", "CANCELLED");
    private static final Pattern PRICE_PATTERN = Pattern.compile("(\\d[\\d\\.]*)");

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private MakeupArtistRepository makeupArtistRepository;

    @Autowired
    private ProfileRepository profileRepository;

    /**
     * Xử lý đặt lịch mới - kiểm tra dữ liệu, xác thực dịch vụ và tính tổng tiền.
     */
    public Booking createBooking(Booking booking) {
        validateBookingRequest(booking);

        booking.setCustomerName(booking.getCustomerName().trim());
        booking.setCustomerPhone(booking.getCustomerPhone().trim());
        booking.setCustomerEmail(booking.getCustomerEmail().trim().toLowerCase(Locale.ROOT));
        booking.setMessage(booking.getMessage() == null ? null : booking.getMessage().trim());

        WeddingService selectedService = serviceRepository.findById(booking.getServiceId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy gói dịch vụ đã chọn."));

        if (booking.getPhotographerId() != null) {
            Profile photographer = profileRepository.findById(booking.getPhotographerId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhiếp ảnh gia đã chọn."));
        }

        if (booking.getMakeupArtistId() != null) {
            MakeupArtist makeupArtist = makeupArtistRepository.findById(booking.getMakeupArtistId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chuyên gia makeup đã chọn."));
        }

        applyServiceSpecificRules(booking, selectedService);
        booking.setStatus("PENDING");
        booking.setPaymentStatus("UNPAID");
        booking.setTotalPrice(parsePrice(selectedService.getPriceRange()));

        return bookingRepository.save(booking);
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAllByOrderByCreatedAtDesc();
    }

    public Booking updateStatus(Long id, String newStatus) {
        if (newStatus == null || newStatus.isBlank()) {
            throw new IllegalArgumentException("Trạng thái không được để trống.");
        }

        String normalizedStatus = newStatus.trim().toUpperCase(Locale.ROOT);
        if (!ALLOWED_STATUSES.contains(normalizedStatus)) {
            throw new IllegalArgumentException("Trạng thái không hợp lệ. Chỉ chấp nhận PENDING, APPROVED hoặc CANCELLED.");
        }

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với ID: " + id));

        booking.setStatus(normalizedStatus);
        return bookingRepository.save(booking);
    }

    public List<?> getMockWeddingServices() {
        return serviceRepository.findAll();
    }

    public List<Profile> getMockPhotographers() {
        return profileRepository.findAll();
    }

    public List<?> getMockMakeupArtists() {
        return makeupArtistRepository.findAll();
    }

    private void validateBookingRequest(Booking booking) {
        if (booking == null) {
            throw new IllegalArgumentException("Dữ liệu đặt lịch không hợp lệ.");
        }

        if (booking.getCustomerName() == null || booking.getCustomerName().trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập họ và tên.");
        }

        if (booking.getCustomerPhone() == null || booking.getCustomerPhone().trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập số điện thoại.");
        }

        if (booking.getCustomerEmail() == null || booking.getCustomerEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập email.");
        }

        if (!booking.getCustomerEmail().trim().matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException("Email không đúng định dạng.");
        }

        if (booking.getBookingDate() == null) {
            throw new IllegalArgumentException("Vui lòng chọn ngày dự kiến.");
        }

        LocalDate tomorrow = LocalDate.now().plusDays(1);
        if (booking.getBookingDate().isBefore(tomorrow)) {
            throw new IllegalArgumentException("Ngày đặt lịch phải từ ngày mai trở đi.");
        }

        if (booking.getServiceId() == null) {
            throw new IllegalArgumentException("Vui lòng chọn gói dịch vụ.");
        }
    }

    private void applyServiceSpecificRules(Booking booking, WeddingService selectedService) {
        String serviceText = buildServiceText(selectedService);

        boolean isDressOnlyService = serviceText.contains("váy") || serviceText.contains("dress");
        boolean needsPhotographer = serviceText.contains("chụp")
                || serviceText.contains("photo")
                || serviceText.contains("album")
                || serviceText.contains("concept")
                || serviceText.contains("studio");
        boolean needsMakeup = serviceText.contains("makeup")
                || serviceText.contains("trang điểm")
                || serviceText.contains("chụp")
                || serviceText.contains("photo")
                || serviceText.contains("album")
                || serviceText.contains("concept")
                || serviceText.contains("studio");

        if (isDressOnlyService) {
            booking.setPhotographerId(null);
            booking.setMakeupArtistId(null);
            return;
        }

        if (!needsPhotographer) {
            booking.setPhotographerId(null);
        }

        if (!needsMakeup) {
            booking.setMakeupArtistId(null);
        }
    }

    private String buildServiceText(WeddingService selectedService) {
        StringBuilder builder = new StringBuilder();
        if (selectedService.getTitle() != null) {
            builder.append(selectedService.getTitle()).append(' ');
        }
        if (selectedService.getShortDescription() != null) {
            builder.append(selectedService.getShortDescription()).append(' ');
        }
        if (selectedService.getDetailedDescription() != null) {
            builder.append(selectedService.getDetailedDescription());
        }
        return builder.toString().toLowerCase(Locale.ROOT);
    }

    private Double parsePrice(String priceRange) {
        if (priceRange == null || priceRange.isBlank()) {
            return 0.0;
        }

        Matcher matcher = PRICE_PATTERN.matcher(priceRange);
        if (!matcher.find()) {
            return 0.0;
        }

        String numericPart = matcher.group(1).replace(".", "");
        try {
            return BigDecimal.valueOf(Long.parseLong(numericPart)).doubleValue();
        } catch (NumberFormatException exception) {
            return 0.0;
        }
    }
}
