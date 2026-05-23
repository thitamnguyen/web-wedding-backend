package com.example.demo.service;

import com.example.demo.model.Booking;
import com.example.demo.model.WeddingService;
import com.example.demo.model.Profile;
import com.example.demo.repository.BookingRepository;
import com.example.demo.repository.ServiceRepository;
import com.example.demo.repository.MakeupArtistRepository;
import com.example.demo.repository.ProfileRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

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

    /**
     * Hàm xử lý đặt lịch mới - Đã bổ sung tự động tính và lưu giá tiền
     */
    public Booking createBooking(Booking booking) {
        booking.setStatus("PENDING");
        booking.setPaymentStatus("UNPAID");

        try {
            // 1. Dựa vào serviceId từ form gửi lên, tìm thông tin Gói dịch vụ trong database
            if (booking.getServiceId() != null) {
                WeddingService selectedService = serviceRepository.findById(booking.getServiceId()).orElse(null);

                if (selectedService != null && selectedService.getPriceRange() != null) {
                    // 2. Tiến hành bóc tách chuỗi (Ví dụ từ "25.000.000 VNĐ" chuyển thành số 25000000.0)
                    String priceStr = selectedService.getPriceRange()
                            .replace(".", "")      // Xóa dấu chấm phân tách
                            .replace(" VNĐ", "")   // Xóa chữ VNĐ
                            .trim();               // Xóa khoảng trắng thừa

                    double calculatedPrice = Double.parseDouble(priceStr);

                    // 3. Gán giá trị tiền thật tính được vào đối tượng booking trước khi lưu
                    booking.setTotalPrice(calculatedPrice);
                }
            }
        } catch (Exception e) {
            // Nếu có lỗi trong quá trình bóc tách chuỗi (ví dụ gói ghi 'Giá liên hệ' không có số), mặc định giữ giá bằng 0
            booking.setTotalPrice(0.0);
            System.err.println("Lỗi tự động tính giá tiền đơn đặt lịch: " + e.getMessage());
        }

        // 4. Lưu xuống database bảng bookings
        return bookingRepository.save(booking);
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public Booking updateStatus(Long id, String newStatus) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với ID: " + id));
        booking.setStatus(newStatus.toUpperCase());
        return bookingRepository.save(booking);
    }

    // 1. Trả về danh sách gói dịch vụ cưới
    public List<?> getMockWeddingServices() {
        return serviceRepository.findAll();
    }

    // 2. Trả về danh sách Photographer từ bảng Profiles chuẩn thực tế
    public List<Profile> getMockPhotographers() {
        return profileRepository.findAll();
    }

    // 3. Trả về danh sách Chuyên gia trang điểm từ bảng beauty_experts
    public List<?> getMockMakeupArtists() {
        return makeupArtistRepository.findAll();
    }
}