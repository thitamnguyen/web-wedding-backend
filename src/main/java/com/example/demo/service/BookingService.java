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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BookingService {

    // 🌟 ĐÃ XÓA DÒNG @Autowired BookingService ĐỂ TRÁNH CRASH VÒNG LẶP TUẦN HOÀN

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
     * Hàm xử lý đặt lịch mới - Đã tối ưu hóa luồng tự động duyệt, kiểm tra bận lịch và gửi email thông báo
     */
    public Booking createBooking(Booking booking) {
        // --- 1. LOGIC KIỂM TRA TRÙNG LỊCH NGAY KHI ĐẶT ---
        if (booking.getPhotographerId() != null && booking.getBookingDate() != null) {
            boolean isPhotoBusy = bookingRepository.existsByPhotographerIdAndBookingDateAndStatus(
                    booking.getPhotographerId(), booking.getBookingDate(), "CONFIRMED"
            );
            if (isPhotoBusy) {
                throw new RuntimeException("Nhiếp ảnh gia này đã có lịch chụp vào ngày " + booking.getBookingDate() + " rồi! Vui lòng chọn ngày khác hoặc thợ khác.");
            }
        }

        if (booking.getMakeupArtistId() != null && booking.getBookingDate() != null) {
            boolean isMakeupBusy = bookingRepository.existsByMakeupArtistIdAndBookingDateAndStatus(
                    booking.getMakeupArtistId(), booking.getBookingDate(), "CONFIRMED"
            );
            if (isMakeupBusy) {
                throw new RuntimeException("Chuyên gia Makeup này đã có lịch trang điểm vào ngày " + booking.getBookingDate() + " rồi! Vui lòng đổi ngày hoặc chọn Artist khác.");
            }
        }

        // --- 2. CẤP PHÁT TRẠNG THÁI TỰ ĐỘNG DUYỆT LUÔN ---
        booking.setStatus("CONFIRMED");
        booking.setPaymentStatus("UNPAID");

        // --- 3. TỰ ĐỘNG TÍNH GIÁ TIỀN TỪ GÓI DỊCH VỤ ---
        try {
            if (booking.getServiceId() != null) {
                WeddingService selectedService = serviceRepository.findById(booking.getServiceId()).orElse(null);
                if (selectedService != null) {
                    String rawPrice = selectedService.getPriceRange();
                    if (rawPrice != null && !rawPrice.trim().isEmpty()) {
                        String cleanPriceStr = rawPrice.replaceAll("[^0-9]", "");
                        if (!cleanPriceStr.isEmpty()) {
                            double calculatedPrice = Double.parseDouble(cleanPriceStr);
                            booking.setTotalPrice(calculatedPrice);
                        }
                    }
                }
            }
        } catch (Exception e) {
            booking.setTotalPrice(0.0);
            System.err.println("❌ Lỗi tự động tính giá tiền đơn đặt lịch: " + e.getMessage());
        }

        // --- 4. LƯU XUỐNG DATABASE ---
        Booking savedBooking = bookingRepository.save(booking);

        // --- 5. TỰ ĐỘNG GỬI EMAIL THÔNG BÁO CHO KHÁCH HÀNG ---
        try {
            if (savedBooking.getCustomerEmail() != null && !savedBooking.getCustomerEmail().isEmpty()) {
                emailService.sendBookingConfirmationEmail(savedBooking);
            }
        } catch (Exception e) {
            System.err.println("❌ Lỗi kích hoạt gửi email tự động: " + e.getMessage());
        }

        return savedBooking;
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public Booking updateStatus(Long id, String newStatus) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn đặt lịch với ID: " + id));

        if ("CONFIRMED".equalsIgnoreCase(newStatus)) {
            if (booking.getPhotographerId() != null) {
                boolean isPhotoBusy = bookingRepository.existsByPhotographerIdAndBookingDateAndStatus(
                        booking.getPhotographerId(), booking.getBookingDate(), "CONFIRMED"
                );
                if (isPhotoBusy) {
                    throw new RuntimeException("🚨 Lỗi: Nhiếp ảnh gia này đã bị trùng lịch chụp vào ngày " + booking.getBookingDate() + "!");
                }
            }

            if (booking.getMakeupArtistId() != null) {
                boolean isMakeupBusy = bookingRepository.existsByMakeupArtistIdAndBookingDateAndStatus(
                        booking.getMakeupArtistId(), booking.getBookingDate(), "CONFIRMED"
                );
                if (isMakeupBusy) {
                    throw new RuntimeException("🚨 Lỗi: Chuyên gia Makeup này đã có lịch trang điểm vào ngày " + booking.getBookingDate() + "!");
                }
            }
        }

        booking.setStatus(newStatus.toUpperCase());
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

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        long pendingCount = bookingRepository.countByStatus("PENDING");
        long confirmedCount = bookingRepository.countByStatus("CONFIRMED");
        long doneCount = bookingRepository.countByStatus("DONE");
        long cancelledCount = bookingRepository.countByStatus("CANCELLED");

        // 🌟 FIX LỖI: Gọi hàm findByStatus(status) không phân trang để tính toán chuẩn tổng doanh thu của toàn hệ thống
        List<Booking> doneBookings = bookingRepository.findByStatus("DONE");
        double totalRevenue = 0.0;
        for (Booking b : doneBookings) {
            if (b.getTotalPrice() != null) {
                totalRevenue += b.getTotalPrice();
            }
        }

        stats.put("totalBookings", bookingRepository.count());
        stats.put("pendingBookings", pendingCount);
        stats.put("confirmedBookings", confirmedCount);
        stats.put("doneBookings", doneCount);
        stats.put("cancelledBookings", cancelledCount);
        stats.put("totalRevenue", totalRevenue);

        return stats;
    }

    public List<Booking> trackBookingByPhone(String phone) {
        return bookingRepository.findByCustomerPhoneOrderByBookingDateDesc(phone);
    }

    // 🌟 FIX LỖI ÉP KIỂU: Hàm trả về đối tượng phân trang Page<Booking> chuẩn chỉ của Spring
    public Page<Booking> getBookingsWithFilter(String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        if (status == null || status.trim().isEmpty() || "ALL".equalsIgnoreCase(status)) {
            return bookingRepository.findAll(pageable);
        }

        return bookingRepository.findByStatus(status, pageable);
    }

    public List<java.time.LocalDate> getPhotographerBusyDates(Long photographerId) {
        return bookingRepository.findBusyDatesForPhotographer(photographerId);
    }

    public List<java.time.LocalDate> getMakeupArtistBusyDates(Long makeupArtistId) {
        return bookingRepository.findBusyDatesForMakeupArtist(makeupArtistId);
    }
}