package com.example.demo.config;

import com.example.demo.model.BlogPost;
import com.example.demo.repository.BlogPostRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class BlogPostDataInitializer implements CommandLineRunner {

    private final BlogPostRepository blogPostRepository;

    public BlogPostDataInitializer(BlogPostRepository blogPostRepository) {
        this.blogPostRepository = blogPostRepository;
    }

    @Override
    public void run(String... args) {
        normalizeExistingPosts();

        if (blogPostRepository.count() > 0) {
            return;
        }

        blogPostRepository.saveAll(List.of(
                createPost(
                        "dia-diem",
                        "Địa điểm",
                        "5 địa điểm chụp cưới đẹp cho album lãng mạn",
                        "dia-diem-5-dia-diem-chup-cuoi-dep",
                        "Tổng hợp những bối cảnh chụp cưới dễ lên hình, có ánh sáng đẹp và phù hợp nhiều phong cách khác nhau.",
                        "Từ studio tối giản đến resort ven biển, mỗi địa điểm đều có ưu và nhược điểm riêng. Bài viết này giúp bạn chọn bối cảnh phù hợp với concept và ngân sách.",
                        "https://images.unsplash.com/photo-1519741497674-611481863552?q=80&w=1200&auto=format&fit=crop",
                        "LUXEAI Studio",
                        "Wedding Editorial Team",
                        6,
                        "địa điểm, chụp cưới, album cưới",
                        true,
                        LocalDateTime.now().minusDays(5)
                ),
                createPost(
                        "concept",
                        "Concept",
                        "3 concept ảnh cưới đang được yêu thích nhất",
                        "concept-3-concept-anh-cuoi-duoc-yeu-thich",
                        "Gợi ý những phong cách ảnh cưới có tính thẩm mỹ cao và dễ triển khai cho nhiều cặp đôi.",
                        "Màu sắc, trang phục và ánh sáng là ba yếu tố quyết định cảm xúc của bộ ảnh. Hãy xem cách kết hợp các yếu tố này để tạo ra bộ hình sang trọng nhưng vẫn tự nhiên.",
                        "https://images.unsplash.com/photo-1522673607200-1648832cee98?q=80&w=1200&auto=format&fit=crop",
                        "LUXEAI Studio",
                        "Creative Director",
                        5,
                        "concept, ảnh cưới, xu hướng",
                        true,
                        LocalDateTime.now().minusDays(3)
                ),
                createPost(
                        "kinh-nghiem",
                        "Chia sẻ kinh nghiệm",
                        "7 kinh nghiệm cần biết trước ngày chụp cưới",
                        "kinh-nghiem-7-kinh-nghiem-truoc-ngay-chup-cuoi",
                        "Những lưu ý quan trọng giúp buổi chụp diễn ra suôn sẻ, tiết kiệm thời gian và hạn chế phát sinh chi phí.",
                        "Chuẩn bị trang phục, lịch trình, đạo cụ và tâm lý trước buổi chụp sẽ giúp bạn thoải mái hơn. Đây là checklist ngắn gọn nhưng rất hữu ích cho các cặp đôi sắp chụp ảnh cưới.",
                        "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?q=80&w=1200&auto=format&fit=crop",
                        "LUXEAI Studio",
                        "Wedding Consultant",
                        7,
                        "kinh nghiệm, chuẩn bị cưới, checklist",
                        true,
                        LocalDateTime.now().minusDays(1)
                )
        ));
    }

    private void normalizeExistingPosts() {
        Map<String, String> categoryKeyMap = new LinkedHashMap<>();
        categoryKeyMap.put("địa điểm", "dia-diem");
        categoryKeyMap.put("dia diem", "dia-diem");
        categoryKeyMap.put("xu hướng cưới", "concept");
        categoryKeyMap.put("xu huong cuoi", "concept");
        categoryKeyMap.put("concept album", "concept");
        categoryKeyMap.put("kinh nghiệm cưới", "kinh-nghiem");
        categoryKeyMap.put("kinh nghiem cuoi", "kinh-nghiem");
        categoryKeyMap.put("chia sẻ kinh nghiệm", "kinh-nghiem");
        categoryKeyMap.put("chia se kinh nghiem", "kinh-nghiem");

        blogPostRepository.findAll().forEach(post -> {
            String category = post.getCategory();
            String normalizedKey = normalizeCategoryKey(category, categoryKeyMap);
            String normalizedLabel = normalizeCategoryLabel(normalizedKey, post.getCategoryLabel());

            boolean changed = false;
            if (normalizedKey != null && !normalizedKey.equals(category)) {
                post.setCategory(normalizedKey);
                changed = true;
            }
            if (normalizedLabel != null && !normalizedLabel.equals(post.getCategoryLabel())) {
                post.setCategoryLabel(normalizedLabel);
                changed = true;
            }

            if (changed) {
                blogPostRepository.save(post);
            }
        });
    }

    private String normalizeCategoryKey(String category, Map<String, String> categoryKeyMap) {
        if (category == null || category.isBlank()) {
            return "dia-diem";
        }

        String trimmed = category.trim().toLowerCase();
        String mapped = categoryKeyMap.get(trimmed);
        if (mapped != null) {
            return mapped;
        }

        return switch (trimmed) {
            case "dia-diem", "concept", "kinh-nghiem" -> trimmed;
            default -> "dia-diem";
        };
    }

    private String normalizeCategoryLabel(String categoryKey, String existingLabel) {
        if (categoryKey == null) {
            return existingLabel != null && !existingLabel.isBlank() ? existingLabel : "Địa điểm";
        }

        return switch (categoryKey) {
            case "concept" -> "Concept";
            case "kinh-nghiem" -> "Chia sẻ kinh nghiệm";
            default -> "Địa điểm";
        };
    }

    private BlogPost createPost(
            String category,
            String categoryLabel,
            String title,
            String slug,
            String excerpt,
            String content,
            String coverImageUrl,
            String authorName,
            String authorTitle,
            Integer readTimeMinutes,
            String tags,
            Boolean published,
            LocalDateTime publishedAt
    ) {
        BlogPost post = new BlogPost();
        post.setCategory(category);
        post.setCategoryLabel(categoryLabel);
        post.setTitle(title);
        post.setSlug(slug);
        post.setExcerpt(excerpt);
        post.setContent(content);
        post.setCoverImageUrl(coverImageUrl);
        post.setAuthorName(authorName);
        post.setAuthorTitle(authorTitle);
        post.setReadTimeMinutes(readTimeMinutes);
        post.setTags(tags);
        post.setPublished(published);
        post.setPublishedAt(publishedAt);
        return post;
    }
}
