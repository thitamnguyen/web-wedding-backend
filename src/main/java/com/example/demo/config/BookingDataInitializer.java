package com.example.demo.config;

import com.example.demo.model.Booking;
import com.example.demo.repository.BookingRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Order(40)
public class BookingDataInitializer implements CommandLineRunner {

    private final BookingRepository bookingRepository;

    public BookingDataInitializer(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @Override
    public void run(String... args) {
        List<Booking> existing = new ArrayList<>(bookingRepository.findAll());
        Set<String> existingEmails = existing.stream()
                .map(Booking::getCustomerEmail)
                .filter(email -> email != null && !email.isBlank())
                .collect(Collectors.toSet());

        boolean changed = false;
        for (Booking sample : buildSamples()) {
            if (!existingEmails.contains(sample.getCustomerEmail())) {
                existing.add(sample);
                changed = true;
            }
        }

        if (changed) {
            bookingRepository.saveAll(existing);
        }
    }

    private List<Booking> buildSamples() {
        LocalDate today = LocalDate.now();
        return List.of(
                create(
                        1L,
                        "Nguyen Thu Ha",
                        "0901000001",
                        "ha@example.com",
                        today.plusDays(2),
                        "PENDING",
                        "UNPAID",
                        1L,
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
                        2L,
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
                        3L,
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
                        1L,
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
