package com.example.demo.config;

import com.example.demo.model.Booking;
import com.example.demo.model.MakeupArtist;
import com.example.demo.model.Profile;
import com.example.demo.repository.BookingRepository;
import com.example.demo.repository.MakeupArtistRepository;
import com.example.demo.repository.ProfileRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Order(20)
public class StaffRevenueBackfillInitializer implements CommandLineRunner {

    private final BookingRepository bookingRepository;
    private final ProfileRepository profileRepository;
    private final MakeupArtistRepository makeupArtistRepository;

    public StaffRevenueBackfillInitializer(
            BookingRepository bookingRepository,
            ProfileRepository profileRepository,
            MakeupArtistRepository makeupArtistRepository
    ) {
        this.bookingRepository = bookingRepository;
        this.profileRepository = profileRepository;
        this.makeupArtistRepository = makeupArtistRepository;
    }

    @Override
    public void run(String... args) {
        List<Booking> completedBookings = bookingRepository.findAll().stream()
                .filter(this::isCompletedBooking)
                .toList();

        Map<Long, BigDecimal> photographerRevenue = new HashMap<>();
        Map<Long, BigDecimal> makeupRevenue = new HashMap<>();

        for (Booking booking : completedBookings) {
            BigDecimal revenue = BigDecimal.valueOf(booking.getTotalPrice() != null ? booking.getTotalPrice() : 0.0);
            if (revenue.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            boolean hasPhotographer = booking.getPhotographerId() != null;
            boolean hasMakeup = booking.getMakeupArtistId() != null;
            int participantCount = (hasPhotographer ? 1 : 0) + (hasMakeup ? 1 : 0);
            if (participantCount == 0) {
                continue;
            }

            BigDecimal share = revenue.divide(BigDecimal.valueOf(participantCount), 2, RoundingMode.HALF_UP);

            if (hasPhotographer) {
                photographerRevenue.merge(booking.getPhotographerId(), share, BigDecimal::add);
            }
            if (hasMakeup) {
                makeupRevenue.merge(booking.getMakeupArtistId(), share, BigDecimal::add);
            }
        }

        List<Profile> profiles = profileRepository.findAll();
        profiles.forEach(profile -> {
            BigDecimal total = photographerRevenue.getOrDefault(profile.getUserId(), BigDecimal.ZERO);
            profile.setTotalRevenue(total);
        });
        profileRepository.saveAll(profiles);

        List<MakeupArtist> artists = makeupArtistRepository.findAll();
        artists.forEach(artist -> {
            BigDecimal total = makeupRevenue.getOrDefault(artist.getId(), BigDecimal.ZERO);
            artist.setTotalRevenue(total);
        });
        makeupArtistRepository.saveAll(artists);
    }

    private boolean isCompletedBooking(Booking booking) {
        String status = booking.getStatus();
        if (status == null) {
            return false;
        }
        String normalized = status.trim().toUpperCase();
        return "DONE".equals(normalized) || "COMPLETED".equals(normalized);
    }
}
