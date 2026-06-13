package com.example.demo.config;

import com.example.demo.model.Profile;
import com.example.demo.repository.ProfileRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class ProfileDataInitializer implements CommandLineRunner {

    private final ProfileRepository profileRepository;

    public ProfileDataInitializer(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    @Override
    public void run(String... args) {
        List<Profile> profiles = profileRepository.findAll();
        if (profiles.isEmpty()) {
            profileRepository.saveAll(defaultProfiles());
            return;
        }

        for (int i = 0; i < profiles.size(); i++) {
            Profile profile = profiles.get(i);
            if (profile.getAvatarUrl() == null || profile.getAvatarUrl().isBlank()) {
                profile.setAvatarUrl(defaultAvatar(i));
            }
            if (profile.getExperienceYears() == null) {
                profile.setExperienceYears(defaultExperience(i));
            }
            if (profile.getRating() == null) {
                profile.setRating(defaultRating(i));
            }
            if (profile.getReviewCount() == null) {
                profile.setReviewCount(defaultReviewCount(i));
            }
            if (profile.getFeaturedWork() == null || profile.getFeaturedWork().isBlank()) {
                profile.setFeaturedWork(defaultFeaturedWork(i));
            }
        }

        profileRepository.saveAll(profiles);
    }

    private List<Profile> defaultProfiles() {
        return List.of(
                createProfile(1L, "Nguyễn Thành Nam", "Wedding Photographer", "Fine Art Documentary", "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?q=80&w=1200&auto=format&fit=crop", 12, BigDecimal.valueOf(4.9), 128, "The Bloom Atelier"),
                createProfile(2L, "Trần Minh Khoa", "Wedding Photographer", "Minimal White Room", "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?q=80&w=1200&auto=format&fit=crop", 10, BigDecimal.valueOf(4.8), 96, "Minimal White Room"),
                createProfile(3L, "Lê Hoàng Long", "Wedding Photographer", "Sunset Storytelling", "https://images.unsplash.com/photo-1504593811423-6dd665756598?q=80&w=1200&auto=format&fit=crop", 9, BigDecimal.valueOf(4.7), 84, "Da Nang Sunset Story"),
                createProfile(4L, "Phạm Quang Huy", "Wedding Photographer", "Heritage Wedding Day", "https://images.unsplash.com/photo-1507591064344-4c6ce005b128?q=80&w=1200&auto=format&fit=crop", 8, BigDecimal.valueOf(4.6), 72, "Hoi An Heritage Walk")
        );
    }

    private Profile createProfile(Long userId, String fullName, String jobTitle, String style, String avatarUrl, Integer experienceYears, BigDecimal rating, Integer reviewCount, String featuredWork) {
        Profile profile = new Profile();
        profile.setUserId(userId);
        profile.setFullName(fullName);
        profile.setJobTitle(jobTitle);
        profile.setStyle(style);
        profile.setAvatarUrl(avatarUrl);
        profile.setExperienceYears(experienceYears);
        profile.setRating(rating);
        profile.setReviewCount(reviewCount);
        profile.setFeaturedWork(featuredWork);
        return profile;
    }

    private Integer defaultExperience(int index) {
        return switch (index % 4) {
            case 0 -> 12;
            case 1 -> 10;
            case 2 -> 8;
            default -> 6;
        };
    }

    private BigDecimal defaultRating(int index) {
        return switch (index % 4) {
            case 0 -> BigDecimal.valueOf(4.9);
            case 1 -> BigDecimal.valueOf(4.8);
            case 2 -> BigDecimal.valueOf(4.7);
            default -> BigDecimal.valueOf(4.6);
        };
    }

    private Integer defaultReviewCount(int index) {
        return switch (index % 4) {
            case 0 -> 128;
            case 1 -> 96;
            case 2 -> 84;
            default -> 72;
        };
    }

    private String defaultAvatar(int index) {
        return switch (index % 4) {
            case 0 -> "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?q=80&w=1200&auto=format&fit=crop";
            case 1 -> "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?q=80&w=1200&auto=format&fit=crop";
            case 2 -> "https://images.unsplash.com/photo-1504593811423-6dd665756598?q=80&w=1200&auto=format&fit=crop";
            default -> "https://images.unsplash.com/photo-1507591064344-4c6ce005b128?q=80&w=1200&auto=format&fit=crop";
        };
    }

    private String defaultFeaturedWork(int index) {
        return switch (index % 4) {
            case 0 -> "Cinematic Sunset Story";
            case 1 -> "Minimal White Room";
            case 2 -> "Editorial Luxury Look";
            default -> "Heritage Wedding Day";
        };
    }
}
