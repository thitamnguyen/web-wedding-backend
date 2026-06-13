package com.example.demo.service;

import com.example.demo.model.Booking;
import com.example.demo.model.WeddingService;
import com.example.demo.model.Profile;
import com.example.demo.repository.BookingRepository;
import com.example.demo.repository.ServiceRepository;
import com.example.demo.repository.MakeupArtistRepository;
import com.example.demo.repository.ProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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

    /**
     * 1. TẠO ĐƠN MỚI: Trạng thái ban đầu bắt buộc phải là PENDING và UNPAID để hiện mã QR cọc!
     */
    public Booking createBooking(Booking booking) {
        // Kiểm tra trùng lịch chụp của Photographer
        if (booking.getPhotographerId() != null && booking.getBookingDate() != null) {
            boolean isPhotoBusy = bookingRepository.existsByPhotographerIdAndBookingDateAndStatus(
                    booking.getPhotographerId(), booking.getBookingDate(), "CONFIRMED"
            );
            if (isPhotoBusy) {
                throw new RuntimeException("Nhiếp ảnh gia này đã có lịch chụp vào ngày " + booking.getBookingDate() + " rồi! Vui lòng chọn ngày khác.");
            }
        }

        // Kiểm tra trùng lịch làm việc của Makeup Artist
        if (booking.getMakeupArtistId() != null && booking.getBookingDate() != null) {
            boolean isMakeupBusy = bookingRepository.existsByMakeupArtistIdAndBookingDateAndStatus(
                    booking.getMakeupArtistId(), booking.getBookingDate(), "CONFIRMED"
            );
            if (isMakeupBusy) {
                throw new RuntimeException("Chuyên gia Makeup này đã có lịch trang điểm vào ngày " + booking.getBookingDate() + " rồi! Vui lòng chọn người khác.");
            }
        }

        // 🌟 THAY ĐỔI GỐC: Đơn mới đặt ở trạng thái chờ duyệt cọc
        booking.setStatus("PENDING");
        booking.setPaymentStatus("UNPAID");

        // Trích xuất tự động giá tiền từ gói WeddingService
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
            booking.setTotalPrice(0.0);
            System.err.println("❌ Lỗi trích xuất giá dịch vụ: " + e.getMessage());
        }

        Booking savedBooking = bookingRepository.save(booking);

        // Tự động kích hoạt gửi Email thông báo kèm thông tin hướng dẫn đặt cọc
        try {
            if (savedBooking.getCustomerEmail() != null && !savedBooking.getCustomerEmail().isEmpty()) {
                emailService.sendBookingConfirmationEmail(savedBooking);
            }
        } catch (Exception e) {
            System.err.println("❌ Lỗi gửi email tự động: " + e.getMessage());
        }

        return savedBooking;
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    /**
     * 2. CẬP NHẬT TRẠNG THÁI & ĐỒNG BỘ LUỒNG TIỀN THEO HÀNH ĐỘNG CỦA ADMIN
     */
    public Booking updateStatus(Long id, String newStatus) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn đặt lịch với ID: " + id));

        String upperStatus = newStatus.toUpperCase();

        // Kịch bản 1: Xác nhận đã nhận tiền cọc thành công
        if ("CONFIRMED".equals(upperStatus)) {
            // Kiểm tra lại trùng lịch một lần nữa trước khi chốt giữ chỗ chính thức
            if (booking.getPhotographerId() != null) {
                boolean isPhotoBusy = bookingRepository.existsByPhotographerIdAndBookingDateAndStatus(
                        booking.getPhotographerId(), booking.getBookingDate(), "CONFIRMED"
                );
                if (isPhotoBusy) {
                    throw new RuntimeException("🚨 Thợ ảnh đã bị trùng lịch chụp ngày " + booking.getBookingDate() + "!");
                }
            }
            booking.setStatus("CONFIRMED");
            booking.setPaymentStatus("DEPOSITED"); // Cập nhật trạng thái Đã Cọc
        }
        // Kịch bản 2: Hoàn thành buổi chụp, thu đủ 100% tiền đơn hàng
        else if ("DONE".equals(upperStatus) || "COMPLETED".equals(upperStatus)) {
            booking.setStatus("DONE");
            booking.setPaymentStatus("PAID"); // Cập nhật trạng thái Đã thanh toán 100%
        }
        // Kịch bản 3: Hủy lịch hẹn
        else if ("CANCELLED".equals(upperStatus)) {
            booking.setStatus("CANCELLED");
            booking.setPaymentStatus("REFUNDED_OR_VOID");
        }
        // Mặc định cho các trạng thái khác
        else {
            booking.setStatus(upperStatus);
        }

        return bookingRepository.save(booking);
    }

    /**
     * 3. THỐNG KÊ DASHBOARD GỐC - Đồng bộ hóa số liệu trùng khớp 100% với báo cáo tài chính mới
     */
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        long pendingCount = bookingRepository.countByStatus("PENDING");
        long confirmedCount = bookingRepository.countByStatus("CONFIRMED");
        long doneCount = bookingRepository.countByStatus("DONE") + bookingRepository.countByStatus("COMPLETED");
        long cancelledCount = bookingRepository.countByStatus("CANCELLED");

        // Gọi logic tính toán chính thống từ hàm tài chính để đồng bộ số liệu hiển thị
        Map<String, BigDecimal> financeReport = getRevenueReportData();

        stats.put("totalBookings", bookingRepository.count());
        stats.put("pendingBookings", pendingCount);
        stats.put("confirmedBookings", confirmedCount);
        stats.put("doneBookings", doneCount);
        stats.put("cancelledBookings", cancelledCount);
        stats.put("totalRevenue", financeReport.get("actualRevenue").doubleValue()); // Doanh thu thực
        stats.put("totalCashFlow", financeReport.get("totalCashFlow").doubleValue()); // Quỹ két tiền mặt

        return stats;
    }

    /**
     * 4. HÀM TÍNH TOÁN BÁO CÁO DOANH THU & DÒNG TIỀN CHUẨN THỰC TẾ CHUYÊN NGHIỆP
     */
    public Map<String, BigDecimal> getRevenueReportData() {
        List<Booking> allBookings = bookingRepository.findAll();

        // 1. DOANH THU CHỐT SỔ: Chỉ tính khi đơn hàng đã đạt trạng thái DONE hoặc COMPLETED
        double actualRevenue = allBookings.stream()
                .filter(b -> b.getTotalPrice() != null)
                .filter(b -> "DONE".equalsIgnoreCase(b.getStatus()) || "COMPLETED".equalsIgnoreCase(b.getStatus()))
                .mapToDouble(Booking::getTotalPrice)
                .sum();

        // 2. TỔNG QUỸ TIỀN MẶT (KÉT): Tính đúng số tiền thực tế đang nằm trong ví doanh nghiệp
        double totalCashFlow = allBookings.stream()
                .filter(b -> b.getTotalPrice() != null)
                .mapToDouble(b -> {
                    double price = b.getTotalPrice();
                    String status = b.getStatus() != null ? b.getStatus().toUpperCase() : "";
                    String payment = b.getPaymentStatus() != null ? b.getPaymentStatus().toUpperCase() : "";

                    // Đã thanh toán 100% hoặc đã chụp xong hoàn thành đơn
                    if (status.equals("DONE") || status.equals("COMPLETED") || payment.equals("PAID")) {
                        return price;
                    }
                    // Đơn đang giữ lịch chờ chụp nhưng đã được Admin duyệt nhận cọc thành công
                    if (payment.equals("DEPOSITED") || status.equals("CONFIRMED")) {
                        return price * 0.2; // Ghi nhận dòng tiền 20% cọc thực thu
                    }
                    return 0.0;
                })
                .sum();

        Map<String, BigDecimal> report = new HashMap<>();
        report.put("actualRevenue", BigDecimal.valueOf(actualRevenue));
        report.put("totalCashFlow", BigDecimal.valueOf(totalCashFlow));
        return report;
    }

    // --- Giữ nguyên các hàm bổ trợ hệ thống của em ---
    public List<?> getMockWeddingServices() { return serviceRepository.findAll(); }
    public List<Profile> getMockPhotographers() { return profileRepository.findAll(); }
    public List<?> getMockMakeupArtists() { return makeupArtistRepository.findAll(); }
    public List<Booking> trackBookingByPhone(String phone) { return bookingRepository.findByCustomerPhoneOrderByBookingDateDesc(phone); }

    public Page<Booking> getBookingsWithFilter(String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        if (status == null || status.trim().isEmpty() || "ALL".equalsIgnoreCase(status)) {
            return bookingRepository.findAll(pageable);
        }
        return bookingRepository.findByStatus(status, pageable);
    }

    public List<java.time.LocalDate> getPhotographerBusyDates(Long photographerId) { return bookingRepository.findBusyDatesForPhotographer(photographerId); }
    public List<java.time.LocalDate> getMakeupArtistBusyDates(Long makeupArtistId) { return bookingRepository.findBusyDatesForMakeupArtist(makeupArtistId); }
}