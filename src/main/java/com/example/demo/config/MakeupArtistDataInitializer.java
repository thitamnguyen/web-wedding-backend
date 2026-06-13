package com.example.demo.config;

import com.example.demo.model.MakeupArtist;
import com.example.demo.model.MakeupPortfolio;
import com.example.demo.model.MakeupReview;
import com.example.demo.repository.MakeupArtistRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MakeupArtistDataInitializer implements CommandLineRunner {

    private final MakeupArtistRepository makeupArtistRepository;

    public MakeupArtistDataInitializer(MakeupArtistRepository makeupArtistRepository) {
        this.makeupArtistRepository = makeupArtistRepository;
    }

    @Override
    public void run(String... args) {
        List<MakeupArtist> artists = makeupArtistRepository.findAll();
        if (artists.isEmpty()) {
            makeupArtistRepository.saveAll(defaultArtists());
            return;
        }

        boolean changed = false;
        for (int i = 0; i < artists.size(); i++) {
            changed = applyDefaults(artists.get(i), i) || changed;
        }

        if (changed) {
            makeupArtistRepository.saveAll(artists);
        }
    }

    private List<MakeupArtist> defaultArtists() {
        return List.of(
                createArtist(
                        "Ngọc Diệp",
                        "Lead Bridal Makeup Artist",
                        "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?q=80&w=1200&auto=format&fit=crop",
                        "Golden Bridal Award 2024",
                        "Soft Glow Bridal",
                        "Phong cách trang điểm trong veo, sáng da và giữ nét tự nhiên cho cô dâu."
                ),
                createArtist(
                        "Bảo Trân",
                        "Editorial Beauty Artist",
                        "https://images.unsplash.com/photo-1487412720507-e7ab37603c6f?q=80&w=1200&auto=format&fit=crop",
                        "Luxury Beauty Choice",
                        "Editorial Luxe Skin",
                        "Đậm chất thời trang, khối nhẹ và ánh mắt sắc nét cho concept studio."
                ),
                createArtist(
                        "Lan Anh",
                        "Korean Bridal Specialist",
                        "https://images.unsplash.com/photo-1517841905240-472988babdf9?q=80&w=1200&auto=format&fit=crop",
                        "Most Loved Bridal Look",
                        "Korean Glass Skin",
                        "Tông da căng bóng, má hồng mềm và tổng thể thanh lịch cho lễ cưới."
                ),
                createArtist(
                        "Minh Châu",
                        "Classic Glam Artist",
                        "https://images.unsplash.com/photo-1494790108377-be9c29b29330?q=80&w=1200&auto=format&fit=crop",
                        "Wedding Day Signature",
                        "Classic Glam Finish",
                        "Phù hợp tiệc tối, đón khách và những concept cần thần thái sang trọng."
                )
        );
    }

    private MakeupArtist createArtist(String fullName, String jobTitle, String avatarUrl, String award, String specialty, String description) {
        MakeupArtist artist = new MakeupArtist();
        artist.setFullName(fullName);
        artist.setJobTitle(jobTitle);
        artist.setAvatarUrl(avatarUrl);
        artist.setAward(award);
        artist.setSpecialty(specialty);
        artist.setDescription(description);

        List<MakeupPortfolio> portfolios = new ArrayList<>();
        portfolios.add(createPortfolio("https://images.unsplash.com/photo-1519741497674-611481863552?q=80&w=1200&auto=format&fit=crop", "Soft Bridal Glow"));
        portfolios.add(createPortfolio("https://images.unsplash.com/photo-1511285560929-80b456fea0bc?q=80&w=1200&auto=format&fit=crop", "Dewy Skin Finish"));
        portfolios.add(createPortfolio("https://images.unsplash.com/photo-1515934751635-c81c6bc9a2d8?q=80&w=1200&auto=format&fit=crop", "Editorial Detail"));
        attachPortfolios(artist, portfolios);

        List<MakeupReview> reviews = new ArrayList<>();
        reviews.add(createReview("Khánh Linh", 5, "Màu makeup rất bền, chụp cả ngày vẫn tươi và tự nhiên."));
        reviews.add(createReview("Thùy Dung", 5, "Phong cách đúng như ảnh mẫu, lên hình cực kỳ sang."));
        attachReviews(artist, reviews);

        return artist;
    }

    private boolean applyDefaults(MakeupArtist artist, int index) {
        boolean changed = false;

        if (artist.getAvatarUrl() == null || artist.getAvatarUrl().isBlank()) {
            artist.setAvatarUrl(defaultAvatar(index));
            changed = true;
        }
        if (artist.getAward() == null || artist.getAward().isBlank()) {
            artist.setAward(defaultAward(index));
            changed = true;
        }
        if (artist.getSpecialty() == null || artist.getSpecialty().isBlank()) {
            artist.setSpecialty(defaultSpecialty(index));
            changed = true;
        }
        if (artist.getDescription() == null || artist.getDescription().isBlank()) {
            artist.setDescription(defaultDescription(index));
            changed = true;
        }
        if (artist.getPortfolios() == null || artist.getPortfolios().isEmpty()) {
            attachPortfolios(artist, defaultPortfolios(index));
            changed = true;
        }
        if (artist.getReviews() == null || artist.getReviews().isEmpty()) {
            attachReviews(artist, defaultReviews(index));
            changed = true;
        }

        return changed;
    }

    private void attachPortfolios(MakeupArtist artist, List<MakeupPortfolio> portfolios) {
        for (MakeupPortfolio portfolio : portfolios) {
            portfolio.setMakeupArtist(artist);
        }
        artist.setPortfolios(portfolios);
    }

    private void attachReviews(MakeupArtist artist, List<MakeupReview> reviews) {
        for (MakeupReview review : reviews) {
            review.setMakeupArtist(artist);
        }
        artist.setReviews(reviews);
    }

    private MakeupPortfolio createPortfolio(String imageUrl, String styleName) {
        MakeupPortfolio portfolio = new MakeupPortfolio();
        portfolio.setImageUrl(imageUrl);
        portfolio.setStyleName(styleName);
        return portfolio;
    }

    private MakeupReview createReview(String customerName, int rating, String comment) {
        MakeupReview review = new MakeupReview();
        review.setCustomerName(customerName);
        review.setRating(rating);
        review.setComment(comment);
        return review;
    }

    private List<MakeupPortfolio> defaultPortfolios(int index) {
        return switch (index % 4) {
            case 0 -> List.of(
                    createPortfolio("https://images.unsplash.com/photo-1519741497674-611481863552?q=80&w=1200&auto=format&fit=crop", "Soft Bridal Glow"),
                    createPortfolio("https://images.unsplash.com/photo-1511285560929-80b456fea0bc?q=80&w=1200&auto=format&fit=crop", "Morning Ceremony"),
                    createPortfolio("https://images.unsplash.com/photo-1515934751635-c81c6bc9a2d8?q=80&w=1200&auto=format&fit=crop", "Reception Shine")
            );
            case 1 -> List.of(
                    createPortfolio("https://images.unsplash.com/photo-1487412720507-e7ab37603c6f?q=80&w=1200&auto=format&fit=crop", "Editorial Skin"),
                    createPortfolio("https://images.unsplash.com/photo-1515934751635-c81c6bc9a2d8?q=80&w=1200&auto=format&fit=crop", "Luxury Contour"),
                    createPortfolio("https://images.unsplash.com/photo-1524504388940-b1c1722653e1?q=80&w=1200&auto=format&fit=crop", "Runway Bride")
            );
            case 2 -> List.of(
                    createPortfolio("https://images.unsplash.com/photo-1517841905240-472988babdf9?q=80&w=1200&auto=format&fit=crop", "Glass Skin"),
                    createPortfolio("https://images.unsplash.com/photo-1494790108377-be9c29b29330?q=80&w=1200&auto=format&fit=crop", "Romantic Tone"),
                    createPortfolio("https://images.unsplash.com/photo-1519741497674-611481863552?q=80&w=1200&auto=format&fit=crop", "Soft Waves")
            );
            default -> List.of(
                    createPortfolio("https://images.unsplash.com/photo-1500648767791-00dcc994a43e?q=80&w=1200&auto=format&fit=crop", "Classic Glam"),
                    createPortfolio("https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?q=80&w=1200&auto=format&fit=crop", "Evening Luxe"),
                    createPortfolio("https://images.unsplash.com/photo-1504593811423-6dd665756598?q=80&w=1200&auto=format&fit=crop", "Timeless Finish")
            );
        };
    }

    private List<MakeupReview> defaultReviews(int index) {
        return switch (index % 4) {
            case 0 -> List.of(
                    createReview("Khánh Linh", 5, "Makeup rất mịn và nhẹ, chụp hình cả ngày vẫn đẹp."),
                    createReview("Minh Anh", 5, "Tông màu đúng tinh thần lễ cưới của mình.")
            );
            case 1 -> List.of(
                    createReview("Ngọc Hân", 5, "Phong cách editorial cực kỳ sang và hiện đại."),
                    createReview("Diễm Quỳnh", 4, "Lúc chụp lên hình rất rõ khối, rất ưng ý.")
            );
            case 2 -> List.of(
                    createReview("Thảo Vy", 5, "Da căng bóng nhưng không bị nặng mặt, rất tự nhiên."),
                    createReview("Hà My", 5, "Tông trang điểm mềm, hợp với váy cưới lắm.")
            );
            default -> List.of(
                    createReview("Mai Phương", 5, "Thần thái được nâng lên rõ rệt, ảnh nhìn rất đắt giá."),
                    createReview("Yến Nhi", 5, "Rất chuyên nghiệp, chỉnh sửa tinh tế đúng gu.")
            );
        };
    }

    private String defaultAvatar(int index) {
        return switch (index % 4) {
            case 0 -> "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?q=80&w=1200&auto=format&fit=crop";
            case 1 -> "https://images.unsplash.com/photo-1487412720507-e7ab37603c6f?q=80&w=1200&auto=format&fit=crop";
            case 2 -> "https://images.unsplash.com/photo-1517841905240-472988babdf9?q=80&w=1200&auto=format&fit=crop";
            default -> "https://images.unsplash.com/photo-1494790108377-be9c29b29330?q=80&w=1200&auto=format&fit=crop";
        };
    }

    private String defaultAward(int index) {
        return switch (index % 4) {
            case 0 -> "Golden Bridal Award 2024";
            case 1 -> "Luxury Beauty Choice";
            case 2 -> "Most Loved Bridal Look";
            default -> "Wedding Day Signature";
        };
    }

    private String defaultSpecialty(int index) {
        return switch (index % 4) {
            case 0 -> "Soft Glow Bridal";
            case 1 -> "Editorial Luxe Skin";
            case 2 -> "Korean Glass Skin";
            default -> "Classic Glam Finish";
        };
    }

    private String defaultDescription(int index) {
        return switch (index % 4) {
            case 0 -> "Phong cách trang điểm trong veo, sáng da và giữ nét tự nhiên cho cô dâu.";
            case 1 -> "Đậm chất thời trang, khối nhẹ và ánh mắt sắc nét cho concept studio.";
            case 2 -> "Tông da căng bóng, má hồng mềm và tổng thể thanh lịch cho lễ cưới.";
            default -> "Phù hợp tiệc tối, đón khách và những concept cần thần thái sang trọng.";
        };
    }
}
