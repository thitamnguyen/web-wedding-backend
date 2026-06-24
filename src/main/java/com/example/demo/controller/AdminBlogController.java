package com.example.demo.controller;

import com.example.demo.dto.BlogPostUpsertRequest;
import com.example.demo.dto.ImageUploadResult;
import com.example.demo.model.BlogPost;
import com.example.demo.repository.BlogPostRepository;
import com.example.demo.service.CloudinaryService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/admin/blogs")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class AdminBlogController {

    private final BlogPostRepository blogPostRepository;
    private final CloudinaryService cloudinaryService;

    public AdminBlogController(BlogPostRepository blogPostRepository, CloudinaryService cloudinaryService) {
        this.blogPostRepository = blogPostRepository;
        this.cloudinaryService = cloudinaryService;
    }

    @GetMapping
    public List<BlogPost> getAllPosts() {
        return blogPostRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(BlogPost::getPublishedAt,
                        Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<BlogPost> getPostById(@PathVariable Long id) {
        return blogPostRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BlogPost> createPost(@ModelAttribute BlogPostUpsertRequest request) {
        BlogPost blogPost = new BlogPost();
        applyRequest(blogPost, request, null);

        if (blogPost.getPublishedAt() == null) {
            blogPost.setPublishedAt(LocalDateTime.now());
        }
        if (blogPost.getPublished() == null) {
            blogPost.setPublished(Boolean.TRUE);
        }

        return ResponseEntity.ok(blogPostRepository.save(blogPost));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BlogPost> updatePost(@PathVariable Long id, @ModelAttribute BlogPostUpsertRequest request) {
        BlogPost existingPost = blogPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khong tim thay bai viet voi ID: " + id));

        String oldPublicId = existingPost.getPublicId();
        applyRequest(existingPost, request, oldPublicId);

        if (request.getPublishedAt() != null) {
            existingPost.setPublishedAt(request.getPublishedAt());
        } else if (existingPost.getPublishedAt() == null) {
            existingPost.setPublishedAt(LocalDateTime.now());
        }

        BlogPost saved = blogPostRepository.save(existingPost);
        if (request.getCoverImageFile() != null && !request.getCoverImageFile().isEmpty() && oldPublicId != null && !oldPublicId.isBlank()) {
            cloudinaryService.deleteImage(oldPublicId);
        }
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePost(@PathVariable Long id) {
        BlogPost existingPost = blogPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khong tim thay bai viet voi ID: " + id));

        cloudinaryService.deleteImage(existingPost.getPublicId());
        blogPostRepository.delete(existingPost);
        return ResponseEntity.ok().build();
    }

    private void applyRequest(BlogPost blogPost, BlogPostUpsertRequest request, String existingPublicId) {
        blogPost.setTitle(request.getTitle());
        blogPost.setSlug(request.getSlug());
        blogPost.setCategory(request.getCategory());
        blogPost.setCategoryLabel(request.getCategoryLabel());
        blogPost.setExcerpt(request.getExcerpt());
        blogPost.setContent(request.getContent());
        blogPost.setAuthorName(request.getAuthorName());
        blogPost.setAuthorTitle(request.getAuthorTitle());
        blogPost.setReadTimeMinutes(request.getReadTimeMinutes());
        blogPost.setTags(request.getTags());
        blogPost.setPublished(request.getPublished() != null ? request.getPublished() : blogPost.getPublished());
        if (request.getPublishedAt() != null) {
            blogPost.setPublishedAt(request.getPublishedAt());
        }

        ImageUploadResult uploadResult = cloudinaryService.uploadImage(request.getCoverImageFile());
        if (uploadResult != null) {
            blogPost.setCoverImageUrl(uploadResult.getSecureUrl());
            blogPost.setPublicId(uploadResult.getPublicId());
        } else if (existingPublicId != null) {
            blogPost.setPublicId(existingPublicId);
        }
    }
}
