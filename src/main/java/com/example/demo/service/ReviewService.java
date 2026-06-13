package com.example.demo.service;

import com.example.demo.model.Review;
import com.example.demo.model.Booking;
import com.example.demo.repository.ReviewRepository;
import com.example.demo.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private BookingRepository bookingRepository;

    public Review saveReview(Review review) {
        // 1. Kiểm tra đơn hàng có tồn tại không
        Booking booking = bookingRepository.findById(review.getBookingId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn đặt lịch!"));

        // 2. Chặn nếu đơn chưa hoàn thành mà đòi đánh giá
        if (!"DONE".equalsIgnoreCase(booking.getStatus())) {
            throw new RuntimeException("Bạn chỉ có thể đánh giá dịch vụ sau khi buổi chụp đã hoàn thành (DONE)!");
        }

        // 3. Chặn nếu đã đánh giá rồi
        if (reviewRepository.existsByBookingId(review.getBookingId())) {
            throw new RuntimeException("Đơn đặt lịch này đã được gửi đánh giá trước đó rồi!");
        }

        // 4. Kèm ID thợ từ đơn hàng vào bảng review để tính điểm uy tín
        review.setPhotographerId(booking.getPhotographerId());
        review.setMakeupArtistId(booking.getMakeupArtistId());
        review.setCustomerName(booking.getCustomerName());

        return reviewRepository.save(review);
    }
}