//package com.example.demo.config;
//
//import com.example.demo.model.ProductGalleryImage;
//import com.example.demo.model.ProductItem;
//import com.example.demo.repository.ProductGalleryImageRepository;
//import com.example.demo.repository.ProductItemRepository;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.stereotype.Component;
//import org.springframework.core.Ordered;
//import org.springframework.core.annotation.Order;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//
//@Component
//@Order(Ordered.LOWEST_PRECEDENCE)
//public class ProductGalleryImageDataInitializer implements CommandLineRunner {
//
//    private final ProductItemRepository productItemRepository;
//    private final ProductGalleryImageRepository productGalleryImageRepository;
//
//    public ProductGalleryImageDataInitializer(
//            ProductItemRepository productItemRepository,
//            ProductGalleryImageRepository productGalleryImageRepository
//    ) {
//        this.productItemRepository = productItemRepository;
//        this.productGalleryImageRepository = productGalleryImageRepository;
//    }
//
//    @Override
//    public void run(String... args) {
//        List<ProductItem> products = productItemRepository.findAll();
//        if (products.isEmpty()) {
//            return;
//        }
//
//        for (ProductItem product : products) {
//            List<ProductGalleryImage> existingImages = productGalleryImageRepository
//                    .findByProductItemIdOrderBySortOrderAscIdAsc(product.getId());
//            List<String> urls = buildGalleryUrls(product);
//            List<String> mergedUrls = new ArrayList<>();
//            for (ProductGalleryImage existingImage : existingImages) {
//                if (!mergedUrls.contains(existingImage.getImageUrl())) {
//                    mergedUrls.add(existingImage.getImageUrl());
//                }
//            }
//            for (String url : urls) {
//                if (!mergedUrls.contains(url)) {
//                    mergedUrls.add(url);
//                }
//            }
//
//            if (mergedUrls.isEmpty()) {
//                continue;
//            }
//
//            List<ProductGalleryImage> images = new ArrayList<>();
//            for (int index = 0; index < mergedUrls.size(); index++) {
//                ProductGalleryImage image;
//                if (index < existingImages.size()) {
//                    image = existingImages.get(index);
//                } else {
//                    image = new ProductGalleryImage();
//                    image.setProductItem(product);
//                }
//                image.setProductItem(product);
//                image.setImageUrl(mergedUrls.get(index));
//                image.setSortOrder(index);
//                images.add(image);
//            }
//
//            productGalleryImageRepository.saveAll(images);
//        }
//    }
//
//    private List<String> buildGalleryUrls(ProductItem product) {
//        List<String> urls = switch (product.getCategoryKey()) {
//            case "concept-noi-bat" -> List.of(
//                    "https://images.unsplash.com/photo-1519225495810-7517c33c2178?q=80&w=1600&auto=format&fit=crop",
//                    "https://images.unsplash.com/photo-1511285560929-80b456fea0bc?q=80&w=1600&auto=format&fit=crop",
//                    "https://images.unsplash.com/photo-1522673607200-1648832cee98?q=80&w=1600&auto=format&fit=crop",
//                    "https://images.unsplash.com/photo-1519741497674-611481863552?q=80&w=1600&auto=format&fit=crop",
//                    "https://images.unsplash.com/photo-1518131678677-a2d10239bd2d?q=80&w=1600&auto=format&fit=crop",
//                    "https://images.unsplash.com/photo-1520854221256-17451cc331bf?q=80&w=1600&auto=format&fit=crop",
//                    "https://images.unsplash.com/photo-1532712938310-34cb3982ef74?q=80&w=1600&auto=format&fit=crop",
//                    "https://images.unsplash.com/photo-1519167758481-83f550bb49b3?q=80&w=1600&auto=format&fit=crop",
//                    "https://images.unsplash.com/photo-1507914372368-b1b15bfb9d2c?q=80&w=1600&auto=format&fit=crop",
//                    "https://images.unsplash.com/photo-1526894923063-1be7f5ed4e5a?q=80&w=1600&auto=format&fit=crop"
//            );
//            case "album-pre-wedding" -> List.of(
//                    "https://images.unsplash.com/photo-1522673607200-1648832cee98?q=80&w=1600&auto=format&fit=crop",
//                    "https://images.unsplash.com/photo-1519741497674-611481863552?q=80&w=1600&auto=format&fit=crop",
//                    "https://images.unsplash.com/photo-1518131394553-c46756843472?q=80&w=1600&auto=format&fit=crop",
//                    "https://images.unsplash.com/photo-1487412720507-e7ab37603c6f?q=80&w=1600&auto=format&fit=crop",
//                    "https://images.unsplash.com/photo-1519225495810-7517c33c2178?q=80&w=1600&auto=format&fit=crop",
//                    "https://images.unsplash.com/photo-1532712938310-34cb3982ef74?q=80&w=1600&auto=format&fit=crop",
//                    "https://images.unsplash.com/photo-1518131678677-a2d10239bd2d?q=80&w=1600&auto=format&fit=crop",
//                    "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?q=80&w=1600&auto=format&fit=crop"
//            );
//            case "bst-vay-cuoi" -> List.of(
//                    "https://images.unsplash.com/photo-1529634806980-85c3dd6d34ac?q=80&w=1600&auto=format&fit=crop",
//                    "https://images.unsplash.com/photo-1519167758481-83f550bb49b3?q=80&w=1600&auto=format&fit=crop",
//                    "https://images.unsplash.com/photo-1515934751635-c81c6bc9a2d8?q=80&w=1600&auto=format&fit=crop",
//                    "https://images.unsplash.com/photo-1507914372368-b1b15bfb9d2c?q=80&w=1600&auto=format&fit=crop",
//                    "https://images.unsplash.com/photo-1526894923063-1be7f5ed4e5a?q=80&w=1600&auto=format&fit=crop",
//                    "https://images.unsplash.com/photo-1518131678677-a2d10239bd2d?q=80&w=1600&auto=format&fit=crop"
//            );
//            case "album-phong-su-cuoi" -> List.of(
//                    "https://images.unsplash.com/photo-1519225495810-7517c33c2178?q=80&w=1600&auto=format&fit=crop",
//                    "https://images.unsplash.com/photo-1522673607200-1648832cee98?q=80&w=1600&auto=format&fit=crop",
//                    "https://images.unsplash.com/photo-1487412720507-e7ab37603c6f?q=80&w=1600&auto=format&fit=crop",
//                    "https://images.unsplash.com/photo-1518131394553-c46756843472?q=80&w=1600&auto=format&fit=crop",
//                    "https://images.unsplash.com/photo-1519741497674-611481863552?q=80&w=1600&auto=format&fit=crop",
//                    "https://images.unsplash.com/photo-1532712938310-34cb3982ef74?q=80&w=1600&auto=format&fit=crop"
//            );
//            case "bridal-makeup" -> List.of(
//                    "https://images.unsplash.com/photo-1511285560929-80b456fea0bc?q=80&w=1600&auto=format&fit=crop",
//                    "https://images.unsplash.com/photo-1515934751635-c81c6bc9a2d8?q=80&w=1600&auto=format&fit=crop",
//                    "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?q=80&w=1600&auto=format&fit=crop",
//                    "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?q=80&w=1600&auto=format&fit=crop",
//                    "https://images.unsplash.com/photo-1517841905240-472988babdf9?q=80&w=1600&auto=format&fit=crop"
//            );
//            default -> {
//                List<String> fallback = new ArrayList<>();
//                if (product.getCoverImageUrl() != null && !product.getCoverImageUrl().isBlank()) {
//                    fallback.add(product.getCoverImageUrl());
//                }
//                yield fallback;
//            }
//        };
//
//        if (product.getGalleryImages() != null && !product.getGalleryImages().isBlank()) {
//            List<String> legacyImages = List.of(product.getGalleryImages().split(","));
//            List<String> merged = new ArrayList<>();
//            for (String image : legacyImages) {
//                String trimmed = image.trim();
//                if (!trimmed.isEmpty() && !merged.contains(trimmed)) {
//                    merged.add(trimmed);
//                }
//            }
//            for (String image : urls) {
//                if (!merged.contains(image)) {
//                    merged.add(image);
//                }
//            }
//            return merged;
//        }
//
//        return urls;
//    }
//}
