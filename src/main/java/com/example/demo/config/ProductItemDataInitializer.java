package com.example.demo.config;

import com.example.demo.model.ProductItem;
import com.example.demo.repository.ProductItemRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class ProductItemDataInitializer implements CommandLineRunner {

    private final ProductItemRepository productItemRepository;

    public ProductItemDataInitializer(ProductItemRepository productItemRepository) {
        this.productItemRepository = productItemRepository;
    }

    @Override
    public void run(String... args) {
        List<ProductItem> existingItems = productItemRepository.findAll();
        if (!existingItems.isEmpty()) {
            boolean updated = false;
            for (ProductItem item : existingItems) {
                updated = applyRelations(item) || updated;
            }
            if (updated) {
                productItemRepository.saveAll(existingItems);
            }
            return;
        }

        productItemRepository.saveAll(List.of(
                create(
                        "Concept Nổi Bật: The Bloom Atelier",
                        "concept-noi-bat-the-bloom-atelier",
                        "concept-noi-bat",
                        "Concept Nổi Bật",
                        "Bộ concept hoa tươi, ánh sáng mềm và bố cục editorial cho ảnh cưới sang trọng.",
                        "Từ ánh sáng đến hoa tươi đều được phối theo tinh thần sang trọng và giàu cảm xúc.",
                        "https://images.unsplash.com/photo-1519225495810-7517c33c2178?q=80&w=1600&auto=format&fit=crop",
                        "https://images.unsplash.com/photo-1519225495810-7517c33c2178?q=80&w=1600&auto=format&fit=crop,https://images.unsplash.com/photo-1511285560929-80b456fea0bc?q=80&w=1600&auto=format&fit=crop",
                        "Từ 12.500.000đ",
                        "Bán chạy",
                        "2026-06-06T10:00:00",
                        """
                                <p>The Bloom Atelier là concept được thiết kế cho các cặp đôi thích sự thanh lịch, nữ tính và có chiều sâu cảm xúc.</p>
                                <p>Bối cảnh được dựng như một studio nghệ thuật với lớp hoa và vải mềm đan xen, giúp ảnh lên rất có chiều và đẳng cấp.</p>
                                <figure><img src="https://images.unsplash.com/photo-1519225495810-7517c33c2178?q=80&w=1600&auto=format&fit=crop" alt="Concept The Bloom Atelier" /></figure>
                                """
                ),
                create(
                        "Concept Nổi Bật: Minimal White Room",
                        "concept-noi-bat-minimal-white-room",
                        "concept-noi-bat",
                        "Concept Nổi Bật",
                        "Concept tối giản với nền trắng, đường nét sạch và điểm nhấn cảm xúc.",
                        "Phù hợp cặp đôi thích sự tinh gọn, sạch sẽ nhưng vẫn muốn hình ảnh thật thời trang.",
                        "https://images.unsplash.com/photo-1519741497674-611481863552?q=80&w=1600&auto=format&fit=crop",
                        "https://images.unsplash.com/photo-1519741497674-611481863552?q=80&w=1600&auto=format&fit=crop,https://images.unsplash.com/photo-1518131678677-a2d10239bd2d?q=80&w=1600&auto=format&fit=crop",
                        "Từ 9.800.000đ",
                        "Mới",
                        "2026-06-05T10:00:00",
                        """
                                <p>Minimal White Room giữ tinh thần tối giản nhưng không lạnh, lấy cảm xúc hai bạn làm trung tâm khung hình.</p>
                                <figure><img src="https://images.unsplash.com/photo-1519741497674-611481863552?q=80&w=1600&auto=format&fit=crop" alt="Concept Minimal White Room" /></figure>
                                """
                ),
                create(
                        "Album Pre Wedding: Da Nang Sunset Story",
                        "album-pre-wedding-da-nang-sunset-story",
                        "album-pre-wedding",
                        "Album Pre Wedding",
                        "Bộ album ngoại cảnh tại Đà Nẵng với lịch chụp bình minh và hoàng hôn.",
                        "Một ngày chụp đi qua biển, cầu và resort để tạo ra bộ ảnh giàu nhịp điệu.",
                        "https://images.unsplash.com/photo-1522673607200-1648832cee98?q=80&w=1600&auto=format&fit=crop",
                        "https://images.unsplash.com/photo-1522673607200-1648832cee98?q=80&w=1600&auto=format&fit=crop,https://images.unsplash.com/photo-1519741497674-611481863552?q=80&w=1600&auto=format&fit=crop",
                        "Từ 18.500.000đ",
                        "Ngoại cảnh",
                        "2026-06-06T09:30:00",
                        """
                                <p>Da Nang Sunset Story là gói chụp dành cho các cặp đôi muốn kết hợp nhiều bối cảnh trong cùng một ngày.</p>
                                <p>Khởi đầu bằng ánh sáng bình minh và kết thúc bằng hoàng hôn để mỗi khung hình đều mang cảm xúc điện ảnh.</p>
                                <figure><img src="https://images.unsplash.com/photo-1522673607200-1648832cee98?q=80&w=1600&auto=format&fit=crop" alt="Album Pre Wedding Da Nang Sunset Story" /></figure>
                                """
                ),
                create(
                        "Album Pre Wedding: Hoi An Heritage Walk",
                        "album-pre-wedding-hoi-an-heritage-walk",
                        "album-pre-wedding",
                        "Album Pre Wedding",
                        "Hành trình chụp tại phố cổ Hội An với gam vàng và cảm giác hoài cổ.",
                        "Phong cách chụp chậm rãi, gần gũi và rất hợp với áo dài hoặc concept cổ điển.",
                        "https://images.unsplash.com/photo-1518131394553-c46756843472?q=80&w=1600&auto=format&fit=crop",
                        "https://images.unsplash.com/photo-1518131394553-c46756843472?q=80&w=1600&auto=format&fit=crop,https://images.unsplash.com/photo-1532712938310-34cb3982ef74?q=80&w=1600&auto=format&fit=crop",
                        "Từ 16.900.000đ",
                        "Phổ biến",
                        "2026-06-05T09:00:00",
                        """
                                <p>Gói này khai thác chất hoài cổ của Hội An bằng những khung hình gần gũi, chậm rãi, nhiều chi tiết đời thường nhưng vẫn sang.</p>
                                <figure><img src="https://images.unsplash.com/photo-1518131394553-c46756843472?q=80&w=1600&auto=format&fit=crop" alt="Album Pre Wedding Hoi An Heritage Walk" /></figure>
                                """
                ),
                create(
                        "BST Váy Cưới: Royal Pearl Collection",
                        "bst-vay-cuoi-royal-pearl-collection",
                        "bst-vay-cuoi",
                        "BST Váy Cưới",
                        "Bộ sưu tập váy cưới dáng công chúa, đính ngọc trai và chất liệu cao cấp.",
                        "Được thiết kế để tôn dáng nhưng vẫn giữ chuyển động nhẹ và tinh tế khi chụp ảnh hoặc lên sân khấu.",
                        "https://images.unsplash.com/photo-1529634806980-85c3dd6d34ac?q=80&w=1600&auto=format&fit=crop",
                        "https://images.unsplash.com/photo-1529634806980-85c3dd6d34ac?q=80&w=1600&auto=format&fit=crop,https://images.unsplash.com/photo-1519167758481-83f550bb49b3?q=80&w=1600&auto=format&fit=crop",
                        "Từ 8.900.000đ",
                        "Cao cấp",
                        "2026-06-04T11:00:00",
                        """
                                <p>Royal Pearl Collection mang tinh thần sang trọng và nổi bật trên sân khấu cưới.</p>
                                <figure><img src="https://images.unsplash.com/photo-1529634806980-85c3dd6d34ac?q=80&w=1600&auto=format&fit=crop" alt="BST Váy Cưới Royal Pearl Collection" /></figure>
                                """
                ),
                create(
                        "BST Váy Cưới: Silk Satin Modern Line",
                        "bst-vay-cuoi-silk-satin-modern-line",
                        "bst-vay-cuoi",
                        "BST Váy Cưới",
                        "Dòng váy tối giản dành cho cô dâu thích vẻ đẹp hiện đại, thanh lịch.",
                        "Phom rơi tự nhiên, điểm nhấn ở vai, cổ và chất liệu lụa phản sáng vừa đủ để lên ảnh đẹp.",
                        "https://images.unsplash.com/photo-1519167758481-83f550bb49b3?q=80&w=1600&auto=format&fit=crop",
                        "https://images.unsplash.com/photo-1519167758481-83f550bb49b3?q=80&w=1600&auto=format&fit=crop,https://images.unsplash.com/photo-1515934751635-c81c6bc9a2d8?q=80&w=1600&auto=format&fit=crop",
                        "Từ 6.500.000đ",
                        "Best Seller",
                        "2026-06-03T10:15:00",
                        """
                                <p>Silk Satin Modern Line tập trung vào phom rơi tự nhiên, tạo vẻ đẹp thanh lịch và hiện đại.</p>
                                <figure><img src="https://images.unsplash.com/photo-1519167758481-83f550bb49b3?q=80&w=1600&auto=format&fit=crop" alt="BST Váy Cưới Silk Satin Modern Line" /></figure>
                                """
                ),
                create(
                        "Album Phóng sự cưới: Wedding Day Motion",
                        "album-phong-su-cuoi-wedding-day-motion",
                        "album-phong-su-cuoi",
                        "Album Phóng sự cưới",
                        "Bộ ảnh phóng sự cưới ghi lại lễ gia tiên, nghi thức và cảm xúc thực tế.",
                        "Gói tập trung vào kể chuyện hơn là tạo dáng, phù hợp với ngày cưới nhiều cảm xúc.",
                        "https://images.unsplash.com/photo-1519225495810-7517c33c2178?q=80&w=1600&auto=format&fit=crop",
                        "https://images.unsplash.com/photo-1519225495810-7517c33c2178?q=80&w=1600&auto=format&fit=crop,https://images.unsplash.com/photo-1522673607200-1648832cee98?q=80&w=1600&auto=format&fit=crop",
                        "Từ 14.500.000đ",
                        "Phóng sự",
                        "2026-06-06T08:30:00",
                        """
                                <p>Wedding Day Motion không tập trung vào tạo dáng quá nhiều mà chú trọng kể chuyện trong từng khoảnh khắc.</p>
                                <figure><img src="https://images.unsplash.com/photo-1519225495810-7517c33c2178?q=80&w=1600&auto=format&fit=crop" alt="Album Phóng sự cưới Wedding Day Motion" /></figure>
                                """
                ),
                create(
                        "Album Phóng sự cưới: Documentary Signature",
                        "album-phong-su-cuoi-documentary-signature",
                        "album-phong-su-cuoi",
                        "Album Phóng sự cưới",
                        "Gói phóng sự theo phong cách documentary, ưu tiên ánh sáng thật và cảm xúc tự nhiên.",
                        "Bộ ảnh mang nhịp điệu đời thực, thích hợp với cặp đôi muốn giữ đúng không khí ngày cưới.",
                        "https://images.unsplash.com/photo-1487412720507-e7ab37603c6f?q=80&w=1600&auto=format&fit=crop",
                        "https://images.unsplash.com/photo-1487412720507-e7ab37603c6f?q=80&w=1600&auto=format&fit=crop,https://images.unsplash.com/photo-1519741497674-611481863552?q=80&w=1600&auto=format&fit=crop",
                        "Từ 16.800.000đ",
                        "Mới",
                        "2026-06-05T08:45:00",
                        """
                                <p>Documentary Signature cho ra bộ ảnh có nhịp điệu tự nhiên, gần với điện ảnh tài liệu.</p>
                                <figure><img src="https://images.unsplash.com/photo-1487412720507-e7ab37603c6f?q=80&w=1600&auto=format&fit=crop" alt="Album Phóng sự cưới Documentary Signature" /></figure>
                                """
                ),
                create(
                        "Bridal Makeup: Soft Glow Signature",
                        "bridal-makeup-soft-glow-signature",
                        "bridal-makeup",
                        "Bridal Makeup",
                        "Phong cách makeup trong trẻo, sáng da và nổi bật nét đẹp tự nhiên.",
                        "Soft Glow Signature phù hợp với cô dâu thích vẻ đẹp tươi sáng, nhẹ nhàng nhưng vẫn lên hình rất rõ.",
                        "https://images.unsplash.com/photo-1511285560929-80b456fea0bc?q=80&w=1600&auto=format&fit=crop",
                        "https://images.unsplash.com/photo-1511285560929-80b456fea0bc?q=80&w=1600&auto=format&fit=crop,https://images.unsplash.com/photo-1515934751635-c81c6bc9a2d8?q=80&w=1600&auto=format&fit=crop",
                        "Từ 3.500.000đ",
                        "Hot",
                        "2026-06-06T11:30:00",
                        """
                                <p>Soft Glow Signature là phong cách phù hợp nhất cho cô dâu thích vẻ đẹp tươi sáng, nhẹ nhàng nhưng vẫn lên hình rất rõ.</p>
                                <figure><img src="https://images.unsplash.com/photo-1511285560929-80b456fea0bc?q=80&w=1600&auto=format&fit=crop" alt="Bridal Makeup Soft Glow Signature" /></figure>
                                """
                ),
                create(
                        "Bridal Makeup: Editorial Luxe Skin",
                        "bridal-makeup-editorial-luxe-skin",
                        "bridal-makeup",
                        "Bridal Makeup",
                        "Makeup theo hướng editorial, sang hơn, nét hơn và phù hợp chụp studio.",
                        "Dùng nền da mịn, khối nhẹ và điểm nhấn mắt tinh tế để tạo cảm giác thời trang hơn.",
                        "https://images.unsplash.com/photo-1515934751635-c81c6bc9a2d8?q=80&w=1600&auto=format&fit=crop",
                        "https://images.unsplash.com/photo-1515934751635-c81c6bc9a2d8?q=80&w=1600&auto=format&fit=crop,https://images.unsplash.com/photo-1438761681033-6461ffad8d80?q=80&w=1600&auto=format&fit=crop",
                        "Từ 4.200.000đ",
                        "Studio",
                        "2026-06-04T12:00:00",
                        """
                                <p>Editorial Luxe Skin sử dụng nền da mịn, khối nhẹ và điểm nhấn mắt tinh tế để tạo ra vẻ đẹp thời trang hơn.</p>
                                <figure><img src="https://images.unsplash.com/photo-1515934751635-c81c6bc9a2d8?q=80&w=1600&auto=format&fit=crop" alt="Bridal Makeup Editorial Luxe Skin" /></figure>
                                """
                )
        ));
    }

    private boolean applyRelations(ProductItem item) {
        boolean changed = false;

        Long photographerId = switch (item.getSlug()) {
            case "concept-noi-bat-the-bloom-atelier", "album-phong-su-cuoi-wedding-day-motion" -> 1L;
            case "concept-noi-bat-minimal-white-room", "album-phong-su-cuoi-documentary-signature" -> 2L;
            case "album-pre-wedding-da-nang-sunset-story" -> 3L;
            case "album-pre-wedding-hoi-an-heritage-walk" -> 4L;
            default -> null;
        };
        if (photographerId != null && !photographerId.equals(item.getPhotographerId())) {
            item.setPhotographerId(photographerId);
            changed = true;
        }

        Long makeupArtistId = switch (item.getSlug()) {
            case "bridal-makeup-soft-glow-signature" -> 1L;
            case "bridal-makeup-editorial-luxe-skin" -> 2L;
            default -> null;
        };
        if (makeupArtistId != null && !makeupArtistId.equals(item.getMakeupArtistId())) {
            item.setMakeupArtistId(makeupArtistId);
            changed = true;
        }

        return changed;
    }

    private ProductItem create(
            String title,
            String slug,
            String categoryKey,
            String categoryLabel,
            String excerpt,
            String subtitle,
            String coverImageUrl,
            String galleryImages,
            String priceRange,
            String badge,
            String publishedAt,
            String content
    ) {
        ProductItem item = new ProductItem();
        item.setTitle(title);
        item.setSlug(slug);
        item.setCategoryKey(categoryKey);
        item.setCategoryLabel(categoryLabel);
        item.setExcerpt(excerpt);
        item.setContent(content);
        item.setCoverImageUrl(coverImageUrl);
        item.setGalleryImages(galleryImages);
        item.setPriceRange(priceRange);
        item.setBadge(badge);
        item.setPublishedAt(LocalDateTime.parse(publishedAt));
        item.setPublished(true);
        applyRelations(item);
        return item;
    }
}
