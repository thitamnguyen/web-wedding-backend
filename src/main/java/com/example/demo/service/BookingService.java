package com.example.demo.service;

import com.example.demo.model.Booking;
import com.example.demo.model.Profile;
import com.example.demo.model.Promotion;
import com.example.demo.model.WeddingService;
import com.example.demo.repository.BookingRepository;
import com.example.demo.repository.MakeupArtistRepository;
import com.example.demo.repository.ProfileRepository;
import com.example.demo.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private MakeupArtistRepository makeupArtistRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PromotionService promotionService;

    public Booking createBooking(Booking booking) {
        validateBookingDates(booking);
        validateStaffAvailability(booking);

        booking.setStatus("PENDING");
        booking.setPaymentStatus("UNPAID");

        try {
            if (booking.getServiceId() != null) {
                WeddingService selectedService = serviceRepository.findById(booking.getServiceId()).orElse(null);
                if (selectedService != null) {
                    String rawPrice = selectedService.getPriceRange();
                    if (rawPrice != null && !rawPrice.trim().isEmpty()) {
                        String cleanPriceStr = rawPrice.replaceAll("[^0-9]", "");
                        if (!cleanPriceStr.isEmpty()) {
                            booking.setTotalPrice(Double.parseDouble(cleanPriceStr));
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Khong the boc tach so tu priceRange: " + e.getMessage());
            booking.setTotalPrice(0.0);
        }

        if (booking.getTotalPrice() == null || booking.getTotalPrice() <= 0) {
            booking.setTotalPrice(0.0);
        }

        applyPromotionIfAny(booking);

        Booking savedBooking = bookingRepository.save(booking);

        try {
            if (savedBooking.getCustomerEmail() != null && !savedBooking.getCustomerEmail().isEmpty()) {
                emailService.sendBookingConfirmationEmail(savedBooking);
            }
        } catch (Exception e) {
            System.err.println("Error sending auto email: " + e.getMessage());
        }

        return savedBooking;
    }

    private void validateBookingDates(Booking booking) {
        LocalDate today = LocalDate.now();
        LocalDate bookingDate = booking.getBookingDate();
        LocalDate fittingDate = booking.getFittingDate();

        if (bookingDate == null) {
            throw new RuntimeException("Vui long chon ngay to chuc / chup chinh.");
        }

        if (!bookingDate.isAfter(today)) {
            throw new RuntimeException("Ngay to chuc / chup chinh phai lon hon ngay hien tai.");
        }

        if (fittingDate != null && !bookingDate.isAfter(fittingDate)) {
            throw new RuntimeException("Ngay to chuc / chup chinh phai lon hon ngay hen thu vay.");
        }
    }

    private void validateStaffAvailability(Booking booking) {
        LocalDate bookingDate = booking.getBookingDate();
        if (bookingDate == null) {
            return;
        }

        if (booking.getPhotographerId() != null
                && bookingRepository.findBusyDatesForPhotographer(booking.getPhotographerId()).contains(bookingDate)) {
            throw new RuntimeException("Nhiep anh gia da co lich vao ngay " + bookingDate + ". Vui long doi tho hoac de web tu sap xep.");
        }

        if (booking.getMakeupArtistId() != null
                && bookingRepository.findBusyDatesForMakeupArtist(booking.getMakeupArtistId()).contains(bookingDate)) {
            throw new RuntimeException("Tho makeup da co lich vao ngay " + bookingDate + ". Vui long doi tho hoac de web tu sap xep.");
        }
    }

    private void applyPromotionIfAny(Booking booking) {
        if (booking.getPromotionCode() == null || booking.getPromotionCode().isBlank()) {
            booking.setDiscountPercentage(0.0);
            booking.setDiscountAmount(0.0);
            booking.setPromotionName(null);
            return;
        }

        Promotion promotion = promotionService.findPromotionByCode(booking.getPromotionCode()).orElse(null);
        if (promotion == null) {
            throw new RuntimeException("Ma uu dai khong hop le hoac da het han");
        }

        if (promotionService.isPromotionUsedByUser(booking.getUserId(), promotion)) {
            throw new RuntimeException("Uu dai nay da duoc su dung roi.");
        }

        double basePrice = booking.getTotalPrice() != null ? booking.getTotalPrice() : 0.0;
        double discountPercentage = promotion.getDiscountPercentage() != null ? promotion.getDiscountPercentage() : 0.0;
        double discountAmount = basePrice * (discountPercentage / 100.0);
        double finalPrice = Math.max(0.0, basePrice - discountAmount);

        booking.setDiscountPercentage(discountPercentage);
        booking.setDiscountAmount(discountAmount);
        booking.setPromotionName(promotion.getName());
        booking.setPromotionCode(promotion.getCode() != null && !promotion.getCode().isBlank() ? promotion.getCode() : booking.getPromotionCode());
        booking.setTotalPrice(finalPrice);
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    @Transactional
    public Booking updateStatus(Long id, String newStatus) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khong tim thay don dat lich voi ID: " + id));

        String previousStatus = booking.getStatus();
        String upperStatus = newStatus == null ? "" : newStatus.toUpperCase();
        boolean wasCompletedBefore = isCompletedStatus(previousStatus);

        if ("CONFIRMED".equals(upperStatus)) {
            if (booking.getPhotographerId() != null) {
                boolean isPhotoBusy = bookingRepository.existsByPhotographerIdAndBookingDateAndStatus(
                        booking.getPhotographerId(), booking.getBookingDate(), "CONFIRMED"
                );
                if (isPhotoBusy) {
                    throw new RuntimeException("Tho anh da bi trung lich chup ngay " + booking.getBookingDate() + "!");
                }
            }
            booking.setStatus("CONFIRMED");
            booking.setPaymentStatus("DEPOSITED");
        } else if ("DONE".equals(upperStatus) || "COMPLETED".equals(upperStatus)) {
            booking.setStatus("DONE");
            booking.setPaymentStatus("PAID");
        } else if ("CANCELLED".equals(upperStatus)) {
            booking.setStatus("CANCELLED");
            booking.setPaymentStatus("REFUNDED_OR_VOID");
        } else {
            booking.setStatus(upperStatus);
        }

        Booking savedBooking = bookingRepository.save(booking);
        if (!wasCompletedBefore && isCompletedStatus(savedBooking.getStatus())) {
            addRevenueToStaff(savedBooking);
        }
        return savedBooking;
    }

    private boolean isCompletedStatus(String status) {
        if (status == null) {
            return false;
        }
        String normalized = status.trim().toUpperCase();
        return "DONE".equals(normalized) || "COMPLETED".equals(normalized);
    }

    private void addRevenueToStaff(Booking booking) {
        BigDecimal revenue = BigDecimal.valueOf(booking.getTotalPrice() != null ? booking.getTotalPrice() : 0.0);
        if (revenue.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        boolean hasPhotographer = booking.getPhotographerId() != null;
        boolean hasMakeup = booking.getMakeupArtistId() != null;
        int participantCount = (hasPhotographer ? 1 : 0) + (hasMakeup ? 1 : 0);
        if (participantCount == 0) {
            return;
        }

        BigDecimal share = revenue.divide(BigDecimal.valueOf(participantCount), 2, RoundingMode.HALF_UP);

        if (hasPhotographer) {
            profileRepository.findById(booking.getPhotographerId()).ifPresent(profile -> {
                BigDecimal current = profile.getTotalRevenue() != null ? profile.getTotalRevenue() : BigDecimal.ZERO;
                profile.setTotalRevenue(current.add(share));
                profileRepository.save(profile);
            });
        }

        if (hasMakeup) {
            makeupArtistRepository.findById(booking.getMakeupArtistId()).ifPresent(artist -> {
                BigDecimal current = artist.getTotalRevenue() != null ? artist.getTotalRevenue() : BigDecimal.ZERO;
                artist.setTotalRevenue(current.add(share));
                makeupArtistRepository.save(artist);
            });
        }
    }

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        long pendingCount = bookingRepository.countByStatus("PENDING");
        long confirmedCount = bookingRepository.countByStatus("CONFIRMED");
        long doneCount = bookingRepository.countByStatus("DONE") + bookingRepository.countByStatus("COMPLETED");
        long cancelledCount = bookingRepository.countByStatus("CANCELLED");

        Map<String, BigDecimal> financeReport = getRevenueReportData();

        stats.put("totalBookings", bookingRepository.count());
        stats.put("pendingBookings", pendingCount);
        stats.put("confirmedBookings", confirmedCount);
        stats.put("doneBookings", doneCount);
        stats.put("cancelledBookings", cancelledCount);
        stats.put("totalRevenue", financeReport.get("actualRevenue").doubleValue());
        stats.put("totalCashFlow", financeReport.get("totalCashFlow").doubleValue());

        return stats;
    }

    public Map<String, BigDecimal> getRevenueReportData() {
        List<Booking> allBookings = bookingRepository.findAll();

        double actualRevenue = allBookings.stream()
                .filter(b -> b.getTotalPrice() != null)
                .filter(b -> "DONE".equalsIgnoreCase(b.getStatus()) || "COMPLETED".equalsIgnoreCase(b.getStatus()))
                .mapToDouble(Booking::getTotalPrice)
                .sum();

        double totalCashFlow = allBookings.stream()
                .filter(b -> b.getTotalPrice() != null)
                .mapToDouble(b -> {
                    double price = b.getTotalPrice();
                    String status = b.getStatus() != null ? b.getStatus().toUpperCase() : "";
                    String payment = b.getPaymentStatus() != null ? b.getPaymentStatus().toUpperCase() : "";

                    if (status.equals("DONE") || status.equals("COMPLETED") || payment.equals("PAID")) {
                        return price;
                    }
                    if (payment.equals("DEPOSITED") || status.equals("CONFIRMED")) {
                        return price * 0.2;
                    }
                    return 0.0;
                })
                .sum();

        Map<String, BigDecimal> report = new HashMap<>();
        report.put("actualRevenue", BigDecimal.valueOf(actualRevenue));
        report.put("totalCashFlow", BigDecimal.valueOf(totalCashFlow));
        return report;
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

    public List<Booking> trackBookingByPhone(String phone) {
        return bookingRepository.findByCustomerPhoneOrderByBookingDateDesc(phone);
    }

    public Page<Booking> getBookingsWithFilter(String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        if (status == null || status.trim().isEmpty() || "ALL".equalsIgnoreCase(status)) {
            return bookingRepository.findAll(pageable);
        }
        return bookingRepository.findByStatus(status, pageable);
    }

    public List<LocalDate> getPhotographerBusyDates(Long photographerId) {
        return bookingRepository.findBusyDatesForPhotographer(photographerId);
    }

    public List<LocalDate> getMakeupArtistBusyDates(Long makeupArtistId) {
        return bookingRepository.findBusyDatesForMakeupArtist(makeupArtistId);
    }
}
