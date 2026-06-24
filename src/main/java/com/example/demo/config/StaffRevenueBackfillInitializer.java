package com.example.demo.config;

import com.example.demo.model.Booking;
import com.example.demo.model.MakeupArtist;
import com.example.demo.model.Profile;
import com.example.demo.model.User;
import com.example.demo.repository.BookingRepository;
import com.example.demo.repository.MakeupArtistRepository;
import com.example.demo.repository.ProfileRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Order(20)
public class StaffRevenueBackfillInitializer implements CommandLineRunner {

    private final BookingRepository bookingRepository;
    private final ProfileRepository profileRepository;
    private final MakeupArtistRepository makeupArtistRepository;
    private final UserRepository userRepository;

    public StaffRevenueBackfillInitializer(
            BookingRepository bookingRepository,
            ProfileRepository profileRepository,
            MakeupArtistRepository makeupArtistRepository,
            UserRepository userRepository
    ) {
        this.bookingRepository = bookingRepository;
        this.profileRepository = profileRepository;
        this.makeupArtistRepository = makeupArtistRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        List<Booking> completedBookings = bookingRepository.findAll().stream()
                .filter(this::isCompletedBooking)
                .toList();

        Map<Long, BigDecimal> photographerRevenue = new HashMap<>();
        Map<Long, BigDecimal> makeupRevenue = new HashMap<>();
        BigDecimal totalAdminProfit = BigDecimal.ZERO;

        for (Booking booking : completedBookings) {
            BigDecimal revenue = BigDecimal.valueOf(booking.getTotalPrice() != null ? booking.getTotalPrice() : 0.0);
            if (revenue.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            BigDecimal adminShare = revenue.multiply(BigDecimal.valueOf(0.50));
            BigDecimal photographerShare = revenue.multiply(BigDecimal.valueOf(0.25));
            BigDecimal makeupShare = revenue.multiply(BigDecimal.valueOf(0.25));

            if (booking.getPhotographerId() != null) {
                photographerRevenue.merge(booking.getPhotographerId(), photographerShare, BigDecimal::add);
            }
            if (booking.getMakeupArtistId() != null) {
                makeupRevenue.merge(booking.getMakeupArtistId(), makeupShare, BigDecimal::add);
            }
            totalAdminProfit = totalAdminProfit.add(adminShare);
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

        List<User> admins = userRepository.findByRoleName("ROLE_ADMIN");
        for (User admin : admins) {
            admin.setRealProfit(totalAdminProfit);
        }
        userRepository.saveAll(admins);
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
