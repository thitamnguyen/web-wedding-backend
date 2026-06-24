package com.example.demo.controller;

import com.example.demo.dto.ImageUploadResult;
import com.example.demo.dto.ProductDetailResponse;
import com.example.demo.dto.ProductGalleryImageResponse;
import com.example.demo.dto.ProductItemUpsertRequest;
import com.example.demo.dto.ProductReviewRequest;
import com.example.demo.dto.ProductReviewResponse;
import com.example.demo.model.ProductGalleryImage;
import com.example.demo.model.ProductItem;
import com.example.demo.model.ProductReview;
import com.example.demo.repository.ProductGalleryImageRepository;
import com.example.demo.repository.ProductItemRepository;
import com.example.demo.repository.ProductReviewRepository;
import com.example.demo.service.CloudinaryService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/product-items")
@CrossOrigin(origins = "*")
public class ProductItemController {

    private final ProductItemRepository productItemRepository;
    private final ProductGalleryImageRepository productGalleryImageRepository;
    private final ProductReviewRepository productReviewRepository;
    private final CloudinaryService cloudinaryService;

    public ProductItemController(
            ProductItemRepository productItemRepository,
            ProductGalleryImageRepository productGalleryImageRepository,
            ProductReviewRepository productReviewRepository,
            CloudinaryService cloudinaryService
    ) {
        this.productItemRepository = productItemRepository;
        this.productGalleryImageRepository = productGalleryImageRepository;
        this.productReviewRepository = productReviewRepository;
        this.cloudinaryService = cloudinaryService;
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public List<ProductItem> getAllProductsForAdmin() {
        return productItemRepository.findAll();
    }

    @GetMapping
    public List<ProductItem> getPublishedProducts() {
        return attachRatings(productItemRepository.findByPublishedTrueOrderByPublishedAtDesc());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createProductItem(@ModelAttribute ProductItemUpsertRequest request) {
        try {
            ProductItem item = new ProductItem();
            applyRequest(item, request, null);

            if (item.getSlug() == null || item.getSlug().isEmpty()) {
                item.setSlug("prod-" + System.currentTimeMillis());
            }
            item.setPublishedAt(LocalDateTime.now());
            if (item.getPublished() == null) {
                item.setPublished(true);
            }

            if (item.getCategoryKey() != null && item.getCategoryLabel() == null) {
                setCategoryLabel(item);
            }

            ProductItem saved = productItemRepository.save(item);
            replaceGalleryImages(saved, request.getGalleryFiles());
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi thêm sản phẩm: " + e.getMessage());
        }
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateProductItem(@PathVariable Long id, @ModelAttribute ProductItemUpsertRequest request) {
        return productItemRepository.findById(id).map(item -> {
            String oldCoverPublicId = item.getPublicId();
            applyRequest(item, request, oldCoverPublicId);

            if (item.getCategoryKey() != null && (request.getCategoryLabel() == null || request.getCategoryLabel().isBlank())) {
                setCategoryLabel(item);
            }

            ProductItem updated = productItemRepository.save(item);
            if (request.getCoverImageFile() != null && !request.getCoverImageFile().isEmpty() && oldCoverPublicId != null && !oldCoverPublicId.isBlank()) {
                cloudinaryService.deleteImage(oldCoverPublicId);
            }
            if (hasFiles(request.getGalleryFiles())) {
                replaceGalleryImages(updated, request.getGalleryFiles());
            }
            return ResponseEntity.ok(updated);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteProductItem(@PathVariable Long id) {
        return productItemRepository.findById(id).map(item -> {
            cloudinaryService.deleteImage(item.getPublicId());
            deleteGalleryImages(item.getId());
            productItemRepository.delete(item);
            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/category/{categoryKey}")
    public List<ProductItem> getProductsByCategory(@PathVariable String categoryKey) {
        return attachRatings(productItemRepository.findByCategoryKeyAndPublishedTrueOrderByPublishedAtDesc(categoryKey));
    }

    @GetMapping("/photographer/{photographerId}")
    public List<ProductItem> getProductsByPhotographer(@PathVariable Long photographerId) {
        return attachRatings(productItemRepository.findByPhotographerIdAndPublishedTrueOrderByPublishedAtDesc(photographerId));
    }

    @GetMapping("/makeup-artist/{makeupArtistId}")
    public List<ProductItem> getProductsByMakeupArtist(@PathVariable Long makeupArtistId) {
        return attachRatings(productItemRepository.findByMakeupArtistIdAndPublishedTrueOrderByPublishedAtDesc(makeupArtistId));
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ProductDetailResponse> getProductBySlug(@PathVariable String slug) {
        return productItemRepository.findBySlugAndPublishedTrue(slug)
                .map(this::toDetailResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{slug}/reviews")
    public ResponseEntity<?> getProductReviews(@PathVariable String slug) {
        return productItemRepository.findBySlugAndPublishedTrue(slug)
                .map(product -> ResponseEntity.ok(
                        productReviewRepository.findByProductItemIdOrderByCreatedAtDesc(product.getId())
                                .stream()
                                .map(review -> new ProductReviewResponse(
                                        review.getId(),
                                        review.getCustomerName(),
                                        review.getRating(),
                                        review.getComment(),
                                        review.getCreatedAt()
                                ))
                                .toList()
                ))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{slug}/reviews")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<?> createProductReview(@PathVariable String slug, @RequestBody ProductReviewRequest request) {
        return productItemRepository.findBySlugAndPublishedTrue(slug)
                .map(product -> {
                    ProductReview review = new ProductReview();
                    review.setProductItem(product);
                    review.setCustomerName(request.getCustomerName());
                    review.setRating(request.getRating());
                    review.setComment(request.getComment());
                    ProductReview saved = productReviewRepository.save(review);
                    return ResponseEntity.ok(new ProductReviewResponse(
                            saved.getId(),
                            saved.getCustomerName(),
                            saved.getRating(),
                            saved.getComment(),
                            saved.getCreatedAt()
                    ));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{slug}/related")
    public List<ProductItem> getRelatedProducts(@PathVariable String slug) {
        return productItemRepository.findBySlugAndPublishedTrue(slug)
                .map(product -> {
                    List<ProductItem> related = productItemRepository
                            .findTop3ByCategoryKeyAndPublishedTrueAndSlugNotOrderByPublishedAtDesc(product.getCategoryKey(), slug);
                    if (!related.isEmpty()) {
                        return related;
                    }
                    return productItemRepository.findTop3ByPublishedTrueAndSlugNotOrderByPublishedAtDesc(slug);
                })
                .orElseGet(List::of);
    }

    private void applyRequest(ProductItem item, ProductItemUpsertRequest request, String existingPublicId) {
        item.setTitle(request.getTitle());
        item.setCategoryKey(request.getCategoryKey());
        item.setCategoryLabel(request.getCategoryLabel());
        item.setExcerpt(request.getExcerpt());
        item.setContent(request.getContent());
        item.setPriceRange(request.getPriceRange());
        item.setBadge(request.getBadge());
        item.setPhotographerId(request.getPhotographerId());
        item.setMakeupArtistId(request.getMakeupArtistId());
        item.setPublished(request.getPublished() != null ? request.getPublished() : item.getPublished());
        if (request.getSlug() != null && !request.getSlug().isBlank()) {
            item.setSlug(request.getSlug());
        }

        ImageUploadResult uploadResult = cloudinaryService.uploadImage(request.getCoverImageFile());
        if (uploadResult != null) {
            item.setCoverImageUrl(uploadResult.getSecureUrl());
            item.setPublicId(uploadResult.getPublicId());
        } else if (existingPublicId != null) {
            item.setPublicId(existingPublicId);
        }
    }

    private void replaceGalleryImages(ProductItem productItem, MultipartFile[] files) {
        if (!hasFiles(files)) {
            return;
        }

        deleteGalleryImages(productItem.getId());

        int sortOrder = 0;
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }
            ImageUploadResult uploadResult = cloudinaryService.uploadImage(file);
            if (uploadResult == null) {
                continue;
            }

            ProductGalleryImage image = new ProductGalleryImage();
            image.setProductItem(productItem);
            image.setImageUrl(uploadResult.getSecureUrl());
            image.setPublicId(uploadResult.getPublicId());
            image.setSortOrder(sortOrder++);
            productGalleryImageRepository.save(image);
        }
    }

    private void deleteGalleryImages(Long productId) {
        List<ProductGalleryImage> existingImages = productGalleryImageRepository.findByProductItemIdOrderBySortOrderAscIdAsc(productId);
        for (ProductGalleryImage image : existingImages) {
            cloudinaryService.deleteImage(image.getPublicId());
        }
        productGalleryImageRepository.deleteAll(existingImages);
    }

    private boolean hasFiles(MultipartFile[] files) {
        if (files == null || files.length == 0) {
            return false;
        }
        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private void setCategoryLabel(ProductItem item) {
        switch (item.getCategoryKey()) {
            case "concept-noi-bat" -> item.setCategoryLabel("Concept Nổi Bật");
            case "album-pre-wedding" -> item.setCategoryLabel("Album Pre-Wedding");
            case "bst-vay-cuoi" -> item.setCategoryLabel("BST Váy Cưới");
            case "album-phong-su-cuoi" -> item.setCategoryLabel("Phóng Sự Cưới");
            case "bridal-makeup" -> item.setCategoryLabel("Bridal Makeup");
            default -> {
            }
        }
    }

    private ProductDetailResponse toDetailResponse(ProductItem product) {
        List<ProductGalleryImageResponse> galleryImages = productGalleryImageRepository
                .findByProductItemIdOrderBySortOrderAscIdAsc(product.getId())
                .stream()
                .map(this::toGalleryResponse)
                .toList();

        long reviewCount = productReviewRepository.countByProductItemId(product.getId());
        double averageRating = productReviewRepository.averageRating(product.getId());

        ProductDetailResponse response = new ProductDetailResponse(
                product.getId(),
                product.getTitle(),
                product.getSlug(),
                product.getCategoryKey(),
                product.getCategoryLabel(),
                product.getExcerpt(),
                product.getContent(),
                product.getCoverImageUrl(),
                product.getPublicId(),
                product.getPhotographerId(),
                product.getMakeupArtistId(),
                product.getPriceRange(),
                product.getBadge(),
                product.getPublishedAt(),
                product.getPublished(),
                galleryImages
        );
        response.setAverageRating(averageRating);
        response.setReviewCount(reviewCount);
        return response;
    }

    private List<ProductItem> attachRatings(List<ProductItem> items) {
        return items.stream().peek(item -> {
            long reviewCount = productReviewRepository.countByProductItemId(item.getId());
            item.setReviewCount(reviewCount);
            item.setAverageRating(productReviewRepository.averageRating(item.getId()));
        }).toList();
    }

    private ProductGalleryImageResponse toGalleryResponse(ProductGalleryImage image) {
        return new ProductGalleryImageResponse(
                image.getId(),
                image.getImageUrl(),
                image.getPublicId(),
                image.getSortOrder()
        );
    }
}
