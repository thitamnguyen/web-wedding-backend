package com.example.demo.controller;

import com.example.demo.model.Booking;
import com.example.demo.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class BookingController {
    @Autowired
    private com.example.demo.repository.NotificationRepository notificationRepository;
    @Autowired
    private com.example.demo.repository.BookingRepository bookingRepository;
    @Autowired
    private BookingService bookingService;

    @PostMapping("/create")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<?> createBooking(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody Booking booking) {
        try {
            if (booking.getUserId() == null) {
                Long userId = extractUserId(authorization);
                if (userId != null) {
                    booking.setUserId(userId);
                }
            }
            Booking savedBooking = bookingService.createBooking(booking);
            return ResponseEntity.ok(savedBooking);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    private Long extractUserId(String authorization) {
        if (authorization == null || authorization.isBlank()) {
            return null;
        }

        String token = authorization.trim();
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

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAllBookings());
    }
    // 🔥 API BỔ SUNG: Lấy danh sách đơn đặt lịch của một khách hàng cụ thể dựa vào userId
    @GetMapping("/user/{userId}")

    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getBookingsByUserId(@PathVariable Long userId) {
        try {
            // Lọc ra các đơn hàng có UserId trùng với mã khách hàng được truyền vào
            List<com.example.demo.model.Booking> userBookings = bookingRepository.findAll().stream()
                    .filter(b -> userId.equals(b.getUserId()))
                    .collect(java.util.stream.Collectors.toList());
            return ResponseEntity.ok(userBookings);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi khi tải lịch sử đơn hàng: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateBookingStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        try {
            Booking updatedBooking = bookingService.updateStatus(id, status);
            return ResponseEntity.ok(updatedBooking);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/services")
    public ResponseEntity<?> getWeddingServices() {
        return ResponseEntity.ok(bookingService.getMockWeddingServices());
    }

    @GetMapping("/photographers")
    public ResponseEntity<?> getPhotographers() {
        return ResponseEntity.ok(bookingService.getMockPhotographers());
    }

    @GetMapping("/makeup-artists")
    public ResponseEntity<?> getMakeupArtists() {
        return ResponseEntity.ok(bookingService.getMockMakeupArtists());
    }

    @GetMapping("/busy-dates/photographer/{photographerId}")
    public ResponseEntity<?> getBusyDatesForPhotographer(@PathVariable Long photographerId) {
        return ResponseEntity.ok(bookingService.getPhotographerBusyDates(photographerId));
    }

    @GetMapping("/busy-dates/makeup/{makeupArtistId}")
    public ResponseEntity<?> getBusyDatesForMakeupArtist(@PathVariable Long makeupArtistId) {
        return ResponseEntity.ok(bookingService.getMakeupArtistBusyDates(makeupArtistId));
    }

    @GetMapping("/dashboard-stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getDashboardStats() {
        try {
            return ResponseEntity.ok(bookingService.getDashboardStats());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/unread")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUnreadBookings() {
        List<Booking> unread = bookingService.getAllBookings().stream()
                .filter(b -> b.getIsRead() == null || !b.getIsRead())
                .toList();
        return ResponseEntity.ok(unread);
    }

    @PutMapping("/{id}/read")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> markAsRead(@PathVariable Long id) {
        try {
            Booking booking = bookingService.getAllBookings().stream()
                    .filter(b -> b.getId().equals(id))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Khong tim thay don dat lich voi ID cung cap"));
            booking.setIsRead(true);
            return ResponseEntity.ok(Map.of("message", "Da ghi nhan doc thong bao thanh cong"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/track")
    public ResponseEntity<?> trackBooking(@RequestParam String phone) {
        return ResponseEntity.ok(bookingService.trackBookingByPhone(phone));
    }

    @GetMapping("/revenue-report")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getRevenueReport() {
        try {
            Map<String, BigDecimal> reportData = bookingService.getRevenueReportData();
            return ResponseEntity.ok(reportData);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }


    // API Public tiếp nhận dữ liệu Webhook tự động từ SEpay khi tài khoản ngân hàng nổ tiền cọc
    @PostMapping("/public/sepay-webhook")
    public ResponseEntity<?> handleSepayWebhook(@RequestBody Map<String, Object> payload) {
        try {
            // Đọc nội dung chuyển khoản do SEpay gửi sang (trường "content" hoặc "transactionContent")
            String description = (String) payload.get("content");
            if (description == null) {
                description = (String) payload.get("transactionContent");
            }

            if (description == null) {
                return ResponseEntity.badRequest().body("Nội dung chuyển khoản không hợp lệ");
            }

            System.out.println("====== [SEPAY WEBHOOK DETECTED] ======");
            System.out.println("Nội dung tin nhắn: " + description);

            // Chuẩn hóa chuỗi xóa khoảng trắng và chuyển chữ hoa: ví dụ "STUDIOWS 12" -> "STUDIOWS12"
            String cleanDesc = description.toUpperCase().replaceAll("\\s+", "");

            // 🔥 THẦY ĐÃ SỬA: Chuyển từ 'STDBK' sang 'STUDIOWS' để khớp hoàn toàn với giao diện Client hiển thị
            if (cleanDesc.contains("STUDIOWS")) {
                int index = cleanDesc.indexOf("STUDIOWS") + 8; // "STUDIOWS" có 8 ký tự

                // Trích xuất chuỗi số ID phía sau chữ STUDIOWS
                String idStr = cleanDesc.substring(index).replaceAll("[^0-9]", "");

                if (!idStr.isEmpty()) {
                    Long bookingId = Long.parseLong(idStr);
                    java.util.Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);

                    if (bookingOpt.isPresent()) {
                        Booking booking = bookingOpt.get();

                        // Nếu đơn đang chờ thanh toán thì tự động duyệt cọc ngay lập tức
                        if ("PENDING".equals(booking.getStatus())) {
                            booking.setStatus("CONFIRMED"); // Đổi trạng thái sang: Đã xác nhận giữ lịch
                            booking.setPaymentStatus("DEPOSITED"); // Trạng thái tiền: Đã cọc thành công
                            bookingRepository.save(booking);

                            System.out.println("✓ [XỬ LÝ TỰ ĐỘNG THÀNH CÔNG] Lịch đặt #" + bookingId + " đã tự động chuyển sang ĐÃ CỌC!");

                            // 🔔 🔥 ĐOẠN THẦY THÊM: TỰ ĐỘNG BẮN CHUÔNG THÔNG BÁO NỔI SANG CHO ADMIN BIẾT Luôn
                            try {
                                com.example.demo.model.Notification notif = new com.example.demo.model.Notification();
                                notif.setTitle("💸 Khách Cọc Online Thành Công!");
                                notif.setMessage("Hệ thống SEpay vừa duyệt tự động đơn hàng #" + booking.getId() + " của khách " + booking.getCustomerName() + " số tiền cọc qua Chuyển Khoản Ngân Hàng.");
                                notif.setBookingId(booking.getId());
                                notif.setIsRead(false);
                                notif.setCreatedAt(java.time.LocalDateTime.now());

                                notificationRepository.save(notif);
                            } catch (Exception eNotif) {
                                System.err.println("Lỗi tạo thông báo nổi cho Webhook: " + eNotif.getMessage());
                            }

                            return ResponseEntity.ok(Map.of("success", true, "message", "Duyệt cọc tự động thành công"));
                        }
                    }
                }
            }
            return ResponseEntity.ok(Map.of("success", false, "message", "Không tìm thấy mã đặt lịch hợp lệ"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi xử lý webhook hệ thống: " + e.getMessage());
        }
    }
    // --- HÀM BỔ SUNG: XỬ LÝ RIÊNG CHO THU TIỀN MẶT ---
    @PutMapping("/{id}/confirm-cash")
    public ResponseEntity<?> confirmCashPayment(@PathVariable Long id) {
        Optional<Booking> bookingOpt = bookingRepository.findById(id);
        if (bookingOpt.isPresent()) {
            Booking booking = bookingOpt.get();
            // Đảo "CASH" lên trước để an toàn tuyệt đối, không ảnh hưởng code khác
            if ("CASH".equalsIgnoreCase(booking.getPaymentMethod()) && "PENDING".equals(booking.getStatus())) {
                booking.setStatus("CONFIRMED");
                booking.setPaymentStatus("DEPOSITED");
                bookingRepository.save(booking);
                return ResponseEntity.ok(Map.of("message", "Đã xác nhận thu tiền mặt tại quầy!"));
            } else {
                return ResponseEntity.badRequest().body(Map.of("message", "Đơn này không chọn tiền mặt hoặc sai trạng thái!"));
            }
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Không tìm thấy lịch đặt!"));
    }
}
