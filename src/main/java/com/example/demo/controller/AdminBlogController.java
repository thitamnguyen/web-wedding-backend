package com.example.demo.controller;

import com.example.demo.model.BlogPost;
import com.example.demo.repository.BlogPostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;
import java.util.Comparator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin/blogs")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class AdminBlogController {

    @Autowired
    private BlogPostRepository blogPostRepository;

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

    @PostMapping
    public ResponseEntity<BlogPost> createPost(@RequestBody BlogPost blogPost) {
        if (blogPost.getPublishedAt() == null) {
            blogPost.setPublishedAt(LocalDateTime.now());
        }

        if (blogPost.getPublished() == null) {
            blogPost.setPublished(Boolean.TRUE);
        }

        return ResponseEntity.ok(blogPostRepository.save(blogPost));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BlogPost> updatePost(@PathVariable Long id, @RequestBody BlogPost postDetails) {
        BlogPost existingPost = blogPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khong tim thay bai viet voi ID: " + id));

        existingPost.setTitle(postDetails.getTitle());
        existingPost.setSlug(postDetails.getSlug());
        existingPost.setCategory(postDetails.getCategory());
        existingPost.setCategoryLabel(postDetails.getCategoryLabel());
        existingPost.setExcerpt(postDetails.getExcerpt());
        existingPost.setContent(postDetails.getContent());
        existingPost.setCoverImageUrl(postDetails.getCoverImageUrl());
        existingPost.setAuthorName(postDetails.getAuthorName());
        existingPost.setAuthorTitle(postDetails.getAuthorTitle());
        existingPost.setReadTimeMinutes(postDetails.getReadTimeMinutes());
        existingPost.setTags(postDetails.getTags());
        existingPost.setPublished(postDetails.getPublished());

        if (postDetails.getPublishedAt() != null) {
            existingPost.setPublishedAt(postDetails.getPublishedAt());
        } else if (existingPost.getPublishedAt() == null) {
            existingPost.setPublishedAt(LocalDateTime.now());
        }

        return ResponseEntity.ok(blogPostRepository.save(existingPost));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePost(@PathVariable Long id) {
        BlogPost existingPost = blogPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khong tim thay bai viet voi ID: " + id));

        blogPostRepository.delete(existingPost);
        return ResponseEntity.ok().build();
    }
}
