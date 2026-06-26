//package com.example.demo.config;
//
//import com.example.demo.model.ProductItem;
//import com.example.demo.repository.ProductItemRepository;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.stereotype.Component;
//
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Set;
//import java.util.stream.Collectors;
//
//@Component
//@org.springframework.core.annotation.Order(12)
//public class ProductItemMoreDataInitializer implements CommandLineRunner {
//
//    private final ProductItemRepository productItemRepository;
//
//    public ProductItemMoreDataInitializer(ProductItemRepository productItemRepository) {
//        this.productItemRepository = productItemRepository;
//    }
//
//    @Override
//    public void run(String... args) {
//        List<ProductItem> items = new ArrayList<>(productItemRepository.findAll());
//        Set<String> existingSlugs = items.stream()
//                .map(ProductItem::getSlug)
//                .collect(Collectors.toSet());
//
//        boolean changed = false;
//        for (ProductItem item : buildItems()) {
//            if (!existingSlugs.contains(item.getSlug())) {
//                items.add(item);
//                changed = true;
//            }
//        }
//
//        if (changed) {
//            productItemRepository.saveAll(items);
//        }
//    }
//
//    private List<ProductItem> buildItems() {
//        return List.of(
//                create(
//                        "Concept N?i B?t: Ivory Grand Hall",
//                        "concept-noi-bat-ivory-grand-hall",
//                        "concept-noi-bat",
//                        "Concept N?i B?t",
//                        "Concept s?nh l?n t?ng ivory, sang tr?ng v? r?t h?p ?nh c??i cao c?p.",
//                        "B? c?c m?, ?nh s?ng tr?ng m?m v? kh?ng gian r?ng gi?p b? ?nh c? c?m gi?c r?t ??t gi?.",
//                        "https://images.unsplash.com/photo-1519227733644-3910d0c3d0b9?q=80&w=1600&auto=format&fit=crop",
//                        "https://images.unsplash.com/photo-1519227733644-3910d0c3d0b9?q=80&w=1600&auto=format&fit=crop,https://images.unsplash.com/photo-1507914372368-b1b15bfb9d2c?q=80&w=1600&auto=format&fit=crop",
//                        "T? 14.200.000?",
//                        "Premium",
//                        "2026-06-10T10:15:00",
//                        "<p>Ivory Grand Hall ???c thi?t k? cho c?p ??i mu?n m?t concept th?t sang, c? kh?ng gian l?n v? c?m gi?c cao c?p ngay t? ?nh nh?n ??u ti?n.</p>",
//                        1L,
//                        null
//                ),
//                create(
//                        "Concept N?i B?t: Forest Light Editorial",
//                        "concept-noi-bat-forest-light-editorial",
//                        "concept-noi-bat",
//                        "Concept N?i B?t",
//                        "Concept ?nh s?ng xuy?n l?, m?m v? ??m ch?t ?i?n ?nh.",
//                        "R?t h?p v?i nh?ng b? ?nh ngo?i tr?i mu?n gi? c?m gi?c t? nhi?n nh?ng v?n c? b? c?c editorial.",
//                        "https://images.unsplash.com/photo-1511285560929-80b456fea0bc?q=80&w=1600&auto=format&fit=crop",
//                        "https://images.unsplash.com/photo-1511285560929-80b456fea0bc?q=80&w=1600&auto=format&fit=crop,https://images.unsplash.com/photo-1496747611176-843222e1e57c?q=80&w=1600&auto=format&fit=crop",
//                        "T? 12.800.000?",
//                        "Outdoor",
//                        "2026-06-09T09:40:00",
//                        "<p>Forest Light Editorial t?n d?ng ?nh s?ng t? nhi?n, th?ch h?p cho c?p ??i th?ch b? ?nh m?m, s?ch v? c? chi?u s?u.</p>",
//                        2L,
//                        null
//                ),
//                create(
//                        "Concept N?i B?t: Pearl White Studio",
//                        "concept-noi-bat-pearl-white-studio",
//                        "concept-noi-bat",
//                        "Concept N?i B?t",
//                        "Concept studio tr?ng, tinh g?n v? hi?n ??i.",
//                        "D?nh cho b? ?nh t?i gi?n, ch? tr?ng chuy?n ??ng nh? v? th?n th?i c?a c? d?u ch? r?.",
//                        "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?q=80&w=1600&auto=format&fit=crop",
//                        "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?q=80&w=1600&auto=format&fit=crop,https://images.unsplash.com/photo-1519741497674-611481863552?q=80&w=1600&auto=format&fit=crop",
//                        "T? 10.600.000?",
//                        "Studio",
//                        "2026-06-08T11:20:00",
//                        "<p>Pearl White Studio gi? tinh th?n t?i gi?n nh?ng v?n thanh l?ch v? r?t d? l?n ?nh ??p cho c?c c?p ??i y?u phong c?ch s?ch.</p>",
//                        1L,
//                        null
//                ),
//                create(
//                        "BST V?y C??i: Moonlight A-Line",
//                        "bst-vay-cuoi-moonlight-a-line",
//                        "bst-vay-cuoi",
//                        "BST V?y C??i",
//                        "V?y ch? A nh?, t?o c?m gi?c thanh tho?t v? t?n d?ng.",
//                        "D?ng v?y c?n b?ng gi?a s? n? t?nh v? s? hi?n ??i, h?p v?i nhi?u concept ch?p kh?c nhau.",
//                        "https://images.unsplash.com/photo-1519741497674-611481863552?q=80&w=1600&auto=format&fit=crop",
//                        "https://images.unsplash.com/photo-1519741497674-611481863552?q=80&w=1600&auto=format&fit=crop,https://images.unsplash.com/photo-1529634806980-85c3dd6d34ac?q=80&w=1600&auto=format&fit=crop",
//                        "T? 7.600.000?",
//                        "Best choice",
//                        "2026-06-10T08:30:00",
//                        "<p>Moonlight A-Line l? chi?c v?y d? m?c, d? l?n h?nh v? ph? h?p v?i c? d?u th?ch v? ??p nh? nh?ng nh?ng kh?ng ??n ?i?u.</p>",
//                        5L,
//                        null
//                ),
//                create(
//                        "BST V?y C??i: Crystal Veil Dress",
//                        "bst-vay-cuoi-crystal-veil-dress",
//                        "bst-vay-cuoi",
//                        "BST V?y C??i",
//                        "Thi?t k? ??nh nh?, sang v? r?t h?p ?nh c??i t?i gi?n.",
//                        "T?p trung v?o ?? r?i c?a v?i v? ?? s?ng c?a b? m?t ?? v?y l?n ?nh th?t tinh t?.",
//                        "https://images.unsplash.com/photo-1507914372368-b1b15bfb9d2c?q=80&w=1600&auto=format&fit=crop",
//                        "https://images.unsplash.com/photo-1507914372368-b1b15bfb9d2c?q=80&w=1600&auto=format&fit=crop,https://images.unsplash.com/photo-1515934751635-c81c6bc9a2d8?q=80&w=1600&auto=format&fit=crop",
//                        "T? 8.100.000?",
//                        "Luxury",
//                        "2026-06-09T14:00:00",
//                        "<p>Crystal Veil Dress d?nh cho c? d?u mu?n m?t thi?t k? v?a tinh t? v?a c? ?? l?p l?nh nh? ?? n?i b?t trong l? c??i.</p>",
//                        6L,
//                        null
//                ),
//                create(
//                        "BST V?y C??i: Silk Rose Couture",
//                        "bst-vay-cuoi-silk-rose-couture",
//                        "bst-vay-cuoi",
//                        "BST V?y C??i",
//                        "L?a m?m, t?ng h?ng kh?i d?u v? c?m gi?c couture r? n?t.",
//                        "M?t m?u v?y m?m m?i nh?ng v?n c? h?nh kh?i r?, h?p v?i concept m?u ?m v? ?nh s?ng v?ng.",
//                        "https://images.unsplash.com/photo-1526894923063-1be7f5ed4e5a?q=80&w=1600&auto=format&fit=crop",
//                        "https://images.unsplash.com/photo-1526894923063-1be7f5ed4e5a?q=80&w=1600&auto=format&fit=crop,https://images.unsplash.com/photo-1500648767791-00dcc994a43e?q=80&w=1600&auto=format&fit=crop",
//                        "T? 9.300.000?",
//                        "Couture",
//                        "2026-06-08T16:10:00",
//                        "<p>Silk Rose Couture mang s?c th?i l?ng m?n h?n, ph? h?p v?i c? d?u th?ch v? ??p sang v? m?m m?i.</p>",
//                        3L,
//                        null
//                ),
//                create(
//                        "Album Pre Wedding: Seoul Minimal Day",
//                        "album-pre-wedding-seoul-minimal-day",
//                        "album-pre-wedding",
//                        "Album Pre Wedding",
//                        "B? ?nh pre-wedding t?i gi?n theo phong c?ch H?n Qu?c.",
//                        "Gi? b?ng m?u s?ng, b? c?c s?ch v? bi?u c?m t? nhi?n ?? b? ?nh l?n r?t hi?n ??i.",
//                        "https://images.unsplash.com/photo-1518131678677-a2d10239bd2d?q=80&w=1600&auto=format&fit=crop",
//                        "https://images.unsplash.com/photo-1518131678677-a2d10239bd2d?q=80&w=1600&auto=format&fit=crop,https://images.unsplash.com/photo-1532712938310-34cb3982ef74?q=80&w=1600&auto=format&fit=crop",
//                        "T? 17.500.000?",
//                        "Korean style",
//                        "2026-06-10T12:45:00",
//                        "<p>Seoul Minimal Day ph? h?p v?i nh?ng c?p ??i th?ch ?nh s?ng, s?ch, ?t m?u nh?ng v?n r?t th?i trang.</p>",
//                        2L,
//                        null
//                ),
//                create(
//                        "Album Pre Wedding: Lantern Garden Story",
//                        "album-pre-wedding-lantern-garden-story",
//                        "album-pre-wedding",
//                        "Album Pre Wedding",
//                        "B?i c?nh v??n ??n l?ng v? ?nh v?ng nh? nh?ng.",
//                        "T?o c?m gi?c ?m ?p, l?ng m?n v? r?t h?p v?i nh?ng khung h?nh gi?u c?m x?c.",
//                        "https://images.unsplash.com/photo-1487412720507-e7ab37603c6f?q=80&w=1600&auto=format&fit=crop",
//                        "https://images.unsplash.com/photo-1487412720507-e7ab37603c6f?q=80&w=1600&auto=format&fit=crop,https://images.unsplash.com/photo-1519225495810-7517c33c2178?q=80&w=1600&auto=format&fit=crop",
//                        "T? 19.200.000?",
//                        "New",
//                        "2026-06-09T10:50:00",
//                        "<p>Lantern Garden Story ??a c?m gi?c m?m, ?m v? c? t?nh k? chuy?n r? r?ng v?o b? ?nh pre-wedding.</p>",
//                        4L,
//                        null
//                ),
//                create(
//                        "Album Ph?ng s? c??i: Ceremony Motion",
//                        "album-phong-su-cuoi-ceremony-motion",
//                        "album-phong-su-cuoi",
//                        "Album Ph?ng s? c??i",
//                        "T?p trung ghi l?i nghi th?c c??i v?i g?c m?y ?i?n ?nh.",
//                        "M?t b? ph?ng s? nh?p nhanh nh?ng v?n gi? ???c c?m x?c v? t?nh ch?n th?c c?a ng?y c??i.",
//                        "https://images.unsplash.com/photo-1519225495810-7517c33c2178?q=80&w=1600&auto=format&fit=crop",
//                        "https://images.unsplash.com/photo-1519225495810-7517c33c2178?q=80&w=1600&auto=format&fit=crop,https://images.unsplash.com/photo-1487412720507-e7ab37603c6f?q=80&w=1600&auto=format&fit=crop",
//                        "T? 15.800.000?",
//                        "Documentary",
//                        "2026-06-10T09:10:00",
//                        "<p>Ceremony Motion ghi l?i nh?ng kho?nh kh?c nghi th?c theo h??ng documentary nh?ng v?n c? ?? ?i?n ?nh r? r?ng.</p>",
//                        1L,
//                        null
//                ),
//                create(
//                        "Album Ph?ng s? c??i: Family Story Flow",
//                        "album-phong-su-cuoi-family-story-flow",
//                        "album-phong-su-cuoi",
//                        "Album Ph?ng s? c??i",
//                        "Kh?c h?a c?m x?c gia ??nh trong ng?y c??i.",
//                        "??m t?nh k? chuy?n, nhi?u kho?nh kh?c t? nhi?n v? r?t ph? h?p ?? l?u gi? k? ?c tr?n v?n.",
//                        "https://images.unsplash.com/photo-1518131394553-c46756843472?q=80&w=1600&auto=format&fit=crop",
//                        "https://images.unsplash.com/photo-1518131394553-c46756843472?q=80&w=1600&auto=format&fit=crop,https://images.unsplash.com/photo-1522673607200-1648832cee98?q=80&w=1600&auto=format&fit=crop",
//                        "T? 17.100.000?",
//                        "Storytelling",
//                        "2026-06-09T09:05:00",
//                        "<p>Family Story Flow ???c d?ng theo nh?p c?m x?c gia ??nh, ph? h?p nh?ng ng?y c??i nhi?u kho?nh kh?c ch?n th?t.</p>",
//                        2L,
//                        null
//                ),
//                create(
//                        "Bridal Makeup: Rose Beige Glow",
//                        "bridal-makeup-rose-beige-glow",
//                        "bridal-makeup",
//                        "Bridal Makeup",
//                        "T?ng h?ng be m?m, t??i v? r?t h?p ?nh c??i ngo?i tr?i.",
//                        "C?n b?ng gi?a s? tr? trung v? n?t thanh l?ch, gi?p c? d?u n?i b?t nh?ng kh?ng qu? d?y.",
//                        "https://images.unsplash.com/photo-1517841905240-472988babdf9?q=80&w=1600&auto=format&fit=crop",
//                        "https://images.unsplash.com/photo-1517841905240-472988babdf9?q=80&w=1600&auto=format&fit=crop,https://images.unsplash.com/photo-1500648767791-00dcc994a43e?q=80&w=1600&auto=format&fit=crop",
//                        "T? 4.300.000?",
//                        "Soft glam",
//                        "2026-06-10T14:20:00",
//                        "<p>Rose Beige Glow l? ki?u makeup d? ?ng d?ng, d? l?n ?nh v? h?p nhi?u ki?u v?y c??i kh?c nhau.</p>",
//                        null,
//                        1L
//                ),
//                create(
//                        "Bridal Makeup: Couture Soft Wing",
//                        "bridal-makeup-couture-soft-wing",
//                        "bridal-makeup",
//                        "Bridal Makeup",
//                        "Nh?n m?t nh?, ???ng k? m?m v? l?p n?n m?n sang.",
//                        "T?o th?n th?i th?i trang h?n nh?ng v?n gi? ???c c?m gi?c c??i tinh t?, d?u nh?.",
//                        "https://images.unsplash.com/photo-1515934751635-c81c6bc9a2d8?q=80&w=1600&auto=format&fit=crop",
//                        "https://images.unsplash.com/photo-1515934751635-c81c6bc9a2d8?q=80&w=1600&auto=format&fit=crop,https://images.unsplash.com/photo-1438761681033-6461ffad8d80?q=80&w=1600&auto=format&fit=crop",
//                        "T? 4.900.000?",
//                        "Editorial",
//                        "2026-06-09T12:30:00",
//                        "<p>Couture Soft Wing h?p v?i c? d?u mu?n m?t layout makeup s?ng, s?c s?o h?n nh?ng v?n r?t m?m v? sang.</p>",
//                        null,
//                        2L
//                ),
//                create(
//                        "Bridal Makeup: Pearl Nude Signature",
//                        "bridal-makeup-pearl-nude-signature",
//                        "bridal-makeup",
//                        "Bridal Makeup",
//                        "T?ng nude ng?c trai, r?t s?ch v? tinh t?.",
//                        "Ph? h?p v?i l? c??i trong nh?, concept high-end ho?c v?y satin t?i gi?n.",
//                        "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?q=80&w=1600&auto=format&fit=crop",
//                        "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?q=80&w=1600&auto=format&fit=crop,https://images.unsplash.com/photo-1487412720507-e7ab37603c6f?q=80&w=1600&auto=format&fit=crop",
//                        "T? 4.600.000?",
//                        "Luxury nude",
//                        "2026-06-08T15:55:00",
//                        "<p>Pearl Nude Signature ph? h?p v?i c? d?u th?ch layout s?ch, s?ng v? c? c?m gi?c cao c?p r? r?t.</p>",
//                        null,
//                        3L
//                )
//        );
//    }
//
//    private ProductItem create(
//            String title,
//            String slug,
//            String categoryKey,
//            String categoryLabel,
//            String excerpt,
//            String contentText,
//            String coverImageUrl,
//            String galleryImages,
//            String priceRange,
//            String badge,
//            String publishedAt,
//            String content,
//            Long photographerId,
//            Long makeupArtistId
//    ) {
//        ProductItem item = new ProductItem();
//        item.setTitle(title);
//        item.setSlug(slug);
//        item.setCategoryKey(categoryKey);
//        item.setCategoryLabel(categoryLabel);
//        item.setExcerpt(excerpt);
//        item.setContent(content);
//        item.setCoverImageUrl(coverImageUrl);
//        item.setGalleryImages(galleryImages);
//        item.setPriceRange(priceRange);
//        item.setBadge(badge);
//        item.setPublishedAt(LocalDateTime.parse(publishedAt));
//        item.setPublished(true);
//        item.setPhotographerId(photographerId);
//        item.setMakeupArtistId(makeupArtistId);
//        return item;
//    }
//}
