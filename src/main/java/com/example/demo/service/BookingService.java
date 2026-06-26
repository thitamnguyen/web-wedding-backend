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
    private com.example.demo.repository.NotificationRepository notificationRepository;

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

    @Autowired
    private com.example.demo.repository.ServicePackageRepository servicePackageRepository;

    @Autowired
    private com.example.demo.repository.UserRepository userRepository;

    public Booking createBooking(Booking booking) {
        validateBookingDates(booking);
        System.out.println(booking.getPhotographerId());
        // Auto-assign photographer if not specified
        if (booking.getPhotographerId() == null) {
            Long autoPhotoId = autoAssignPhotographer(booking.getBookingDate());
            if (autoPhotoId == null) {
                throw new RuntimeException("Không tìm được nhiếp ảnh gia trống vào ngày bạn chọn.");
            }
            booking.setPhotographerId(autoPhotoId);
        }

        // Auto-assign makeup artist if not specified
        if (booking.getMakeupArtistId() == null) {
            Long autoMakeupId = autoAssignMakeupArtist(booking.getBookingDate());
            if (autoMakeupId == null) {
                throw new RuntimeException("Không tìm được thợ makeup trống vào ngày bạn chọn.");
            }
            booking.setMakeupArtistId(autoMakeupId);
        }

        validateStaffAvailability(booking);

        booking.setStatus("PENDING");
        booking.setPaymentStatus("UNPAID");

        try {
            if (booking.getServicePackageId() != null) {
                com.example.demo.model.ServicePackage selectedPackage = servicePackageRepository.findById(booking.getServicePackageId()).orElse(null);
                if (selectedPackage != null) {
                    booking.setTotalPrice(selectedPackage.getPrice());
                }
            }
        } catch (Exception e) {
            System.out.println("Khong the lay gia tu ServicePackage: " + e.getMessage());
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
        // --- ĐOẠN CODE BỔ SUNG: TỰ ĐỘNG BẮN THÔNG BÁO CHO ADMIN ---
        try {
            com.example.demo.model.Notification notif = new com.example.demo.model.Notification();
            notif.setTitle("📅 Đơn Đặt Lịch Mới!");
            notif.setMessage("Khách hàng " + savedBooking.getCustomerName() + " vừa đặt lịch hẹn chụp ngày " + savedBooking.getBookingDate());
            notif.setBookingId(savedBooking.getId());
            notif.setIsRead(false);
            notif.setCreatedAt(java.time.LocalDateTime.now());

            // Lưu thông báo nổi vào database để quả chuông Admin nhận diện được luôn
            notificationRepository.save(notif);
        } catch (Exception e) {
            System.err.println("Lỗi tự động tạo thông báo đặt lịch: " + e.getMessage());
        }

        // 🔔 🔥 ĐOẠN THẦY THÊM: Bọc try-catch an toàn tuyệt đối, tránh gây crash luồng đặt lịch chính của khách
        try {
            com.example.demo.model.Notification notif = new com.example.demo.model.Notification();
            notif.setTitle("📅 Đơn Đặt Lịch Mới!");
            notif.setMessage("Khách hàng " + savedBooking.getCustomerName() + " vừa đặt lịch hẹn chụp ngày " + savedBooking.getBookingDate());
            notif.setBookingId(savedBooking.getId());
            notif.setIsRead(false);
            notif.setCreatedAt(java.time.LocalDateTime.now());

            notificationRepository.save(notif);
        } catch (Exception e) {
            System.err.println("Log cảnh báo: Lỗi bắn chuông thông báo nhưng đơn hàng vẫn đặt thành công: " + e.getMessage());
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

        // Thực hiện chia tiền chuẩn xác theo tỷ lệ: Admin 50%, Photo 25%, Makeup 25%
        BigDecimal adminShare = revenue.multiply(BigDecimal.valueOf(0.50)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal photographerShare = revenue.multiply(BigDecimal.valueOf(0.25)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal makeupShare = revenue.multiply(BigDecimal.valueOf(0.25)).setScale(2, RoundingMode.HALF_UP);

        // 1. Cộng doanh thu cho Photographer (25%)
        if (booking.getPhotographerId() != null) {
            profileRepository.findById(booking.getPhotographerId()).ifPresent(profile -> {
                BigDecimal current = profile.getTotalRevenue() != null ? profile.getTotalRevenue() : BigDecimal.ZERO;
                profile.setTotalRevenue(current.add(photographerShare));
                profileRepository.save(profile);
            });
        }

        // 2. Cộng doanh thu cho Makeup Artist (25%)
        if (booking.getMakeupArtistId() != null) {
            makeupArtistRepository.findById(booking.getMakeupArtistId()).ifPresent(artist -> {
                BigDecimal current = artist.getTotalRevenue() != null ? artist.getTotalRevenue() : BigDecimal.ZERO;
                artist.setTotalRevenue(current.add(makeupShare));
                makeupArtistRepository.save(artist);
            });
        }

        // 3. Cộng 50% Doanh thu thực tế (real_profit) cho tài khoản Admin
        // Tìm tài khoản có ID = 1 làm tài khoản nhận tiền Admin mặc định
        userRepository.findById(5L).ifPresent(admin -> {
            try {
                // LƯU Ý: Đảm bảo model User/Admin của bạn đã có trường realProfit (kiểu BigDecimal) nhé
                // Nếu tên thuộc tính trong Model khác (ví dụ: doanhThuThucTe), hãy sửa tên hàm get/set cho khớp.
                java.lang.reflect.Method getProfitMethod = admin.getClass().getMethod("getRealProfit");
                BigDecimal currentProfit = (BigDecimal) getProfitMethod.invoke(admin);
                if (currentProfit == null) currentProfit = BigDecimal.ZERO;

                java.lang.reflect.Method setProfitMethod = admin.getClass().getMethod("setRealProfit", BigDecimal.class);
                setProfitMethod.invoke(admin, currentProfit.add(adminShare));

                userRepository.save(admin);
            } catch (Exception e) {
                System.out.println("Lưu ý: Hãy kiểm tra xem Model User đã được thêm thuộc tính 'realProfit' chưa: " + e.getMessage());
            }
        });
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

        BigDecimal totalActualRevenue = financeReport.get("actualRevenue");
        stats.put("totalRevenue", totalActualRevenue.doubleValue());
        stats.put("totalCashFlow", financeReport.get("totalCashFlow").doubleValue());

        // Bổ sung dữ liệu Doanh thu thực tế của admin lên Dashboard bằng 50% tổng doanh thu đơn hoàn thành
        BigDecimal adminRealProfit = totalActualRevenue.multiply(BigDecimal.valueOf(0.50)).setScale(2, RoundingMode.HALF_UP);
        stats.put("realProfit", adminRealProfit.doubleValue());

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

    public Long autoAssignPhotographer(LocalDate bookingDate) {
        if (bookingDate == null) {
            return null;
        }
        List<com.example.demo.model.Profile> allPhotographers = profileRepository.findAll();
        System.out.println("--- BẮT ĐẦU CHECK NGÀY: " + bookingDate + " ---");
        List<com.example.demo.model.Profile> freePhotographers = allPhotographers.stream()
                .filter(p -> {
                    List<LocalDate> busyDates = getPhotographerBusyDates(p.getUserId());
                    return !busyDates.contains(bookingDate);
                })
                .toList();
            System.out.println("Danh sách thợ RẢNH sau khi lọc: "
                + freePhotographers.stream().map(p -> "ID: " + p.getUserId() + " - Doanh thu: " + p.getTotalRevenue()).toList());
        if (freePhotographers.isEmpty()) {
            return null;
        }

        // FIX BUG: So sánh trực tiếp thuộc tính totalRevenue từ object Profile thay vì gọi câu query lỗi của Repository
        com.example.demo.model.Profile selected = freePhotographers.stream()
                .min(java.util.Comparator.comparing(p -> p.getTotalRevenue() != null ? p.getTotalRevenue() : BigDecimal.ZERO))
                .orElse(null);

        return selected != null ? selected.getUserId() : null;
    }

    public Long autoAssignMakeupArtist(LocalDate bookingDate) {
        if (bookingDate == null) {
            return null;
        }
        List<com.example.demo.model.MakeupArtist> allMakeup = makeupArtistRepository.findAll();
        List<com.example.demo.model.MakeupArtist> freeMakeup = allMakeup.stream()
                .filter(m -> {
                    List<LocalDate> busyDates = getMakeupArtistBusyDates(m.getId());
                    return !busyDates.contains(bookingDate);
                })
                .toList();

        if (freeMakeup.isEmpty()) {
            return null;
        }

        // FIX BUG: So sánh trực tiếp thuộc tính totalRevenue từ object MakeupArtist để tìm người có doanh thu thấp nhất
        com.example.demo.model.MakeupArtist selected = freeMakeup.stream()
                .min(java.util.Comparator.comparing(m -> m.getTotalRevenue() != null ? m.getTotalRevenue() : BigDecimal.ZERO))
                .orElse(null);

        return selected != null ? selected.getId() : null;
    }
}