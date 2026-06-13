package com.example.demo.service;

import com.example.demo.model.Booking;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // Sử dụng @Async để việc gửi thư chạy ngầm, không làm nghẽn luồng xử lý chính của khách hàng
    @Async
    public void sendBookingConfirmationEmail(Booking booking) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("studio.damcuoi.test@gmail.com");
            message.setTo(booking.getCustomerEmail());
            message.setSubject("🎉 [Wedding Studio] Xác nhận đặt lịch thành công & Giữ chỗ Ekip!");

            // Tính toán số tiền cọc trực tiếp bằng code
            double totalPrice = booking.getTotalPrice() != null ? booking.getTotalPrice() : 0.0;
            double depositPrice = totalPrice * 0.2; // Đặt cọc giữ chỗ 20%

            String content = "Xin chào " + booking.getCustomerName() + ",\n\n"
                    + "Chúc mừng bạn đã đặt lịch thành công tại Wedding Studio!\n"
                    + "Hệ thống đã tự động phê duyệt lịch hẹn và điều phối nhân sự gán riêng cho ngày trọng đại của bạn.\n\n"
                    + "--- THÔNG TIN CHI TIẾT LỊCH HẸN ---\n"
                    + "• Mã đơn lịch: #WS" + booking.getId() + "\n"
                    + "• Ngày thực hiện: " + booking.getBookingDate() + "\n"
                    + "• Tổng chi phí dịch vụ: " + String.format("%,.0f", totalPrice) + " VNĐ\n"
                    + "• Số tiền cần đặt cọc giữ chỗ (20%): " + String.format("%,.0f", depositPrice) + " VNĐ\n\n"
                    + "📸 Ê-kíp Photographer và Makeup Artist chất lượng nhất đã được gán sẵn sàng.\n"
                    + "Vui lòng hoàn tất quét mã QR thanh toán đặt cọc trên giao diện Website để lịch trình được cam kết giữ chỗ tuyệt đối.\n\n"
                    + "Trân trọng,\n"
                    + "Đội ngũ điều hành Wedding Studio.";

            message.setText(content);
            mailSender.send(message);
            System.out.println("🚀 Email xác nhận tự động đã gửi thành công tới: " + booking.getCustomerEmail());
        } catch (Exception e) {
            System.err.println("❌ Lỗi xảy ra trong quá trình gửi email tự động: " + e.getMessage());
        }
    }
}