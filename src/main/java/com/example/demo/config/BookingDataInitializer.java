//package com.example.demo.config;
//
//import com.example.demo.model.Booking;
//import com.example.demo.repository.BookingRepository;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.core.annotation.Order;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.stereotype.Component;
//
//import java.time.LocalDate;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import java.util.stream.Collectors;
//
//@Component
//@Order(40) // Đảm bảo chạy sau khi các bảng chính đã được tạo
//public class BookingDataInitializer implements CommandLineRunner {
//
//    private final BookingRepository bookingRepository;
//    private final JdbcTemplate jdbcTemplate;
//
//    // Inject thêm JdbcTemplate để kiểm tra và lấy ID thực tế từ database
//    public BookingDataInitializer(BookingRepository bookingRepository, JdbcTemplate jdbcTemplate) {
//        this.bookingRepository = bookingRepository;
//        this.jdbcTemplate = jdbcTemplate;
//    }
//
//    @Override
//    public void run(String... args) {
//        // 1. Kiểm tra và chèn danh mục dịch vụ (service_categories) nếu trống
//        Integer categoryCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM service_categories", Integer.class);
//        if (categoryCount == null || categoryCount == 0) {
//            System.out.println("👉 Bảng service_categories trống. Đang tạo danh mục mẫu...");
//            jdbcTemplate.execute("INSERT INTO service_categories (id, category_code, title, tagline, image_url, sub_title, description) VALUES (1, 'NGAY_CUOI', 'Gói Ngày cưới', 'Trọn gói lễ gia tiên & tiệc cưới', '', 'Dịch vụ ngày cưới', 'Mô tả chi tiết')");
//            jdbcTemplate.execute("INSERT INTO service_categories (id, category_code, title, tagline, image_url, sub_title, description) VALUES (2, 'PRE_WEDDING', 'Gói Pre Wedding', 'Album cưới ngoại cảnh', '', 'Dịch vụ Pre Wedding', 'Mô tả chi tiết')");
//            jdbcTemplate.execute("INSERT INTO service_categories (id, category_code, title, tagline, image_url, sub_title, description) VALUES (3, 'PHONG_SU', 'Quay Phóng sự ngày cưới', 'Cinematic highlight', '', 'Dịch vụ quay phim', 'Mô tả chi tiết')");
//        }
//
//        // 2. Kiểm tra và chèn gói dịch vụ (service_packages) nếu trống
//        Integer packageCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM service_packages", Integer.class);
//        if (packageCount == null || packageCount == 0) {
//            System.out.println("👉 Bảng service_packages trống. Đang tạo gói dịch vụ mẫu...");
//            jdbcTemplate.execute("INSERT INTO service_packages (id, name, price, outfits, makeup, duration, team, products, category_id) VALUES (1, 'Standard Package', 12500000.0, '2 Váy cưới, 2 Vest', '1 Lần trang điểm', '4 Tiếng', '1 Photographer', '1 Album 30 trang', 1)");
//            jdbcTemplate.execute("INSERT INTO service_packages (id, name, price, outfits, makeup, duration, team, products, category_id) VALUES (2, 'Premium Package', 18500000.0, '3 Váy cưới, 3 Vest', '2 Lần trang điểm', '1 Ngày', '1 Photographer, 1 Assistant', '1 Album 40 trang', 2)");
//            jdbcTemplate.execute("INSERT INTO service_packages (id, name, price, outfits, makeup, duration, team, products, category_id) VALUES (3, 'VIP Package', 16800000.0, 'N/A', 'N/A', '1 Ngày', '2 Photographers, 1 Videographer', '1 Phim highlight 5 phút', 3)");
//        }
//
//        // Lấy danh sách ID gói dịch vụ thực tế đang có trong DB
//        List<Integer> validPackageIds = jdbcTemplate.queryForList("SELECT id FROM service_packages", Integer.class);
//
//        if (validPackageIds.isEmpty()) {
//            System.err.println("❌ Không tìm thấy gói dịch vụ nào. Bỏ qua việc khởi tạo Booking mẫu để tránh sập app!");
//            return;
//        }
//
//        // 3. Tiến hành kiểm tra và nạp dữ liệu Booking mẫu
//        List<Booking> existing = new ArrayList<>(bookingRepository.findAll());
//        Set<String> existingEmails = existing.stream()
//                .map(Booking::getCustomerEmail)
//                .filter(email -> email != null && !email.isBlank())
//                .collect(Collectors.toSet());
//
//        boolean changed = false;
//        // Truyền danh sách ID hợp lệ vào hàm build
//        for (Booking sample : buildSamples(validPackageIds)) {
//            if (!existingEmails.contains(sample.getCustomerEmail())) {
//                existing.add(sample);
//                changed = true;
//            }
//        }
//
//        if (changed) {
//            try {
//                bookingRepository.saveAll(existing);
//                System.out.println("✅ Khởi tạo dữ liệu Booking mẫu thành công!");
//            } catch (Exception e) {
//                System.err.println("❌ Lỗi khi lưu Booking: " + e.getMessage());
//            }
//        }
//    }
//
//    private List<Booking> buildSamples(List<Integer> validPackageIds) {
//        LocalDate today = LocalDate.now();
//
//        // Chọn ID an toàn từ DB: Nếu DB không có đủ 3 ID khác nhau, ta sẽ lấy ID đầu tiên gán làm mặc định
//        Integer id1 = validPackageIds.get(0);
//        Integer id2 = validPackageIds.size() > 1 ? validPackageIds.get(1) : id1;
//        Integer id3 = validPackageIds.size() > 2 ? validPackageIds.get(2) : id1;
//
//        return List.of(
//                create(
//                        1L,
//                        "Nguyen Thu Ha",
//                        "0901000001",
//                        "ha@example.com",
//                        today.plusDays(2),
//                        "PENDING",
//                        "UNPAID",
//                        id1, // Thay gán cứng 1L bằng id lấy từ DB
//                        1L,
//                        1L,
//                        12500000.0,
//                        "Tu van concept va chot lich chup."
//                ),
//                create(
//                        1L,
//                        "Tran Minh Khang",
//                        "0901000002",
//                        "khang@example.com",
//                        today.plusDays(5),
//                        "CONFIRMED",
//                        "DEPOSITED",
//                        id2, // Thay gán cứng 2L bằng id lấy từ DB
//                        1L,
//                        1L,
//                        18500000.0,
//                        "Chup pre-wedding boi canh tu nhien."
//                ),
//                create(
//                        1L,
//                        "Le Ngoc Anh",
//                        "0901000003",
//                        "anh@example.com",
//                        today.minusDays(4),
//                        "DONE",
//                        "PAID",
//                        id3, // Thay gán cứng 3L bằng id lấy từ DB
//                        1L,
//                        1L,
//                        16800000.0,
//                        "Da hoan thanh album phong su cuoi."
//                ),
//                create(
//                        1L,
//                        "Pham Bao Chau",
//                        "0901000004",
//                        "chau@example.com",
//                        today.plusDays(1),
//                        "CONFIRMED",
//                        "DEPOSITED",
//                        id1, // Quay lại ID hợp lệ đầu tiên
//                        2L,
//                        2L,
//                        9800000.0,
//                        "Lich trong studio va makeup buoi sang."
//                )
//        );
//    }
//
//    private Booking create(
//            Long userId,
//            String customerName,
//            String customerPhone,
//            String customerEmail,
//            LocalDate bookingDate,
//            String status,
//            String paymentStatus,
//            Integer servicePackageId,
//            Long photographerId,
//            Long makeupArtistId,
//            Double totalPrice,
//            String message
//    ) {
//        Booking booking = new Booking();
//        booking.setUserId(userId);
//        booking.setCustomerName(customerName);
//        booking.setCustomerPhone(customerPhone);
//        booking.setCustomerEmail(customerEmail);
//        booking.setBookingDate(bookingDate);
//        booking.setStatus(status);
//        booking.setPaymentStatus(paymentStatus);
//        booking.setServicePackageId(servicePackageId);
//        booking.setPhotographerId(photographerId);
//        booking.setMakeupArtistId(makeupArtistId);
//        booking.setTotalPrice(totalPrice);
//        booking.setMessage(message);
//        booking.setIsRead(Boolean.TRUE);
//        return booking;
//    }
//}