package com.example.demo.config;

import com.example.demo.model.Booking;
import com.example.demo.repository.BookingRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Order(40) // Đảm bảo chạy sau khi các bảng chính đã được tạo
public class BookingDataInitializer implements CommandLineRunner {

    private final BookingRepository bookingRepository;
    private final JdbcTemplate jdbcTemplate;

    // Inject thêm JdbcTemplate để kiểm tra và lấy ID thực tế từ database
    public BookingDataInitializer(BookingRepository bookingRepository, JdbcTemplate jdbcTemplate) {
        this.bookingRepository = bookingRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        // 1. Kiểm tra xem bảng wedding_services đã có dữ liệu chưa, nếu chưa có thì chèn tạm dữ liệu mồi
        Integer serviceCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM wedding_services", Integer.class);
        if (serviceCount == null || serviceCount == 0) {
            System.out.println("👉 Bảng wedding_services trống. Đang tạo dịch vụ mồi để tránh lỗi Foreign Key...");
            jdbcTemplate.execute("INSERT INTO wedding_services (id, name, price) VALUES (1, 'Gói Chụp Ảnh Cưới Standard', 12500000.0)");
            jdbcTemplate.execute("INSERT INTO wedding_services (id, name, price) VALUES (2, 'Gói Chụp Ảnh Cưới Premium', 18500000.0)");
            jdbcTemplate.execute("INSERT INTO wedding_services (id, name, price) VALUES (3, 'Gói Phóng Sự Cưới VIP', 16800000.0)");
        }

        // Lấy danh sách ID dịch vụ thực tế đang có trong DB
        List<Long> validServiceIds = jdbcTemplate.queryForList("SELECT id FROM wedding_services", Long.class);

        if (validServiceIds.isEmpty()) {
            System.err.println("❌ Không tìm thấy dịch vụ nào. Bỏ qua việc khởi tạo Booking mẫu để tránh sập app!");
            return;
        }

        // 2. Tiến hành kiểm tra và nạp dữ liệu Booking mẫu
        List<Booking> existing = new ArrayList<>(bookingRepository.findAll());
        Set<String> existingEmails = existing.stream()
                .map(Booking::getCustomerEmail)
                .filter(email -> email != null && !email.isBlank())
                .collect(Collectors.toSet());

        boolean changed = false;
        // Truyền danh sách ID hợp lệ vào hàm build
        for (Booking sample : buildSamples(validServiceIds)) {
            if (!existingEmails.contains(sample.getCustomerEmail())) {
                existing.add(sample);
                changed = true;
            }
        }

        if (changed) {
            try {
                bookingRepository.saveAll(existing);
                System.out.println("✅ Khởi tạo dữ liệu Booking mẫu thành công!");
            } catch (Exception e) {
                System.err.println("❌ Lỗi khi lưu Booking: " + e.getMessage());
            }
        }
    }

    private List<Booking> buildSamples(List<Long> validServiceIds) {
        LocalDate today = LocalDate.now();

        // Chọn ID an toàn từ DB: Nếu DB không có đủ 3 ID khác nhau, ta sẽ lấy ID đầu tiên gán làm mặc định
        Long id1 = validServiceIds.get(0);
        Long id2 = validServiceIds.size() > 1 ? validServiceIds.get(1) : id1;
        Long id3 = validServiceIds.size() > 2 ? validServiceIds.get(2) : id1;

        return List.of(
                create(
                        1L,
                        "Nguyen Thu Ha",
                        "0901000001",
                        "ha@example.com",
                        today.plusDays(2),
                        "PENDING",
                        "UNPAID",
                        id1, // Thay gán cứng 1L bằng id lấy từ DB
                        1L,
                        1L,
                        12500000.0,
                        "Tu van concept va chot lich chup."
                ),
                create(
                        1L,
                        "Tran Minh Khang",
                        "0901000002",
                        "khang@example.com",
                        today.plusDays(5),
                        "CONFIRMED",
                        "DEPOSITED",
                        id2, // Thay gán cứng 2L bằng id lấy từ DB
                        1L,
                        1L,
                        18500000.0,
                        "Chup pre-wedding boi canh tu nhien."
                ),
                create(
                        1L,
                        "Le Ngoc Anh",
                        "0901000003",
                        "anh@example.com",
                        today.minusDays(4),
                        "DONE",
                        "PAID",
                        id3, // Thay gán cứng 3L bằng id lấy từ DB
                        1L,
                        1L,
                        16800000.0,
                        "Da hoan thanh album phong su cuoi."
                ),
                create(
                        1L,
                        "Pham Bao Chau",
                        "0901000004",
                        "chau@example.com",
                        today.plusDays(1),
                        "CONFIRMED",
                        "DEPOSITED",
                        id1, // Quay lại ID hợp lệ đầu tiên
                        2L,
                        2L,
                        9800000.0,
                        "Lich trong studio va makeup buoi sang."
                )
        );
    }

    private Booking create(
            Long userId,
            String customerName,
            String customerPhone,
            String customerEmail,
            LocalDate bookingDate,
            String status,
            String paymentStatus,
            Long serviceId,
            Long photographerId,
            Long makeupArtistId,
            Double totalPrice,
            String message
    ) {
        Booking booking = new Booking();
        booking.setUserId(userId);
        booking.setCustomerName(customerName);
        booking.setCustomerPhone(customerPhone);
        booking.setCustomerEmail(customerEmail);
        booking.setBookingDate(bookingDate);
        booking.setStatus(status);
        booking.setPaymentStatus(paymentStatus);
        booking.setServiceId(serviceId);
        booking.setPhotographerId(photographerId);
        booking.setMakeupArtistId(makeupArtistId);
        booking.setTotalPrice(totalPrice);
        booking.setMessage(message);
        booking.setIsRead(Boolean.TRUE);
        return booking;
    }
}