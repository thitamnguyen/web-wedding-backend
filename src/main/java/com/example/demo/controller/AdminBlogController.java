package com.example.demo.controller;

import com.example.demo.model.BlogPost;
import com.example.demo.repository.BlogPostRepository; // Nhớ đổi tên package theo đúng dự án của em
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin/blogs")
@CrossOrigin(origins = "http://localhost:3000") // Cấu hình CORS để React gọi không bị chặn lỗi bảo mật
public class AdminBlogController {


    @Autowired
    private BlogPostRepository blogPostRepository;

    // 1. API: Lấy toàn bộ danh sách bài viết sắp xếp theo ID giảm dần (Bài mới lên trước)
    @GetMapping
    public List<BlogPost> getAllBlogs() {
        return blogPostRepository.findAll();
    }

    // 2. API: Viết bài viết Blog mới
    @PostMapping
    public ResponseEntity<?> createBlogPost(@RequestBody BlogPost blogPost) {
        try {
            blogPost.setPublishedAt(LocalDateTime.now()); // Tự động ghi nhận giờ xuất bản thực tế
            if(blogPost.getCategoryLabel() == null || blogPost.getCategoryLabel().isEmpty()) {
                blogPost.setCategoryLabel("Blog");
            }
            BlogPost savedPost = blogPostRepository.save(blogPost);
            return ResponseEntity.ok(savedPost);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi khi thêm bài viết: " + e.getMessage());
        }
    }

    // 3. API: Sửa thông tin nội dung bài viết cũ
    @PutMapping("/{id}")
    public ResponseEntity<?> updateBlogPost(@PathVariable Long id, @RequestBody BlogPost updatedPost) {
        return blogPostRepository.findById(id).map(existingPost -> {
            existingPost.setTitle(updatedPost.getTitle());
            existingPost.setSlug(updatedPost.getSlug());
            existingPost.setCategory(updatedPost.getCategory());
            existingPost.setCategoryLabel(updatedPost.getCategoryLabel());
            existingPost.setExcerpt(updatedPost.getExcerpt());
            existingPost.setContent(updatedPost.getContent());
            existingPost.setCoverImageUrl(updatedPost.getCoverImageUrl());
            existingPost.setAuthorName(updatedPost.getAuthorName());
            existingPost.setAuthorTitle(updatedPost.getAuthorTitle());
            existingPost.setReadTimeMinutes(updatedPost.getReadTimeMinutes());
            existingPost.setTags(updatedPost.getTags());
            existingPost.setPublished(updatedPost.getPublished());

            BlogPost saved = blogPostRepository.save(existingPost);
            return ResponseEntity.ok(saved);
        }).orElse(ResponseEntity.notFound().build());
    }

    // 4. API: Xóa vĩnh viễn bài viết khỏi hệ thống tạp chí
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBlogPost(@PathVariable Long id) {
        return blogPostRepository.findById(id).map(post -> {
            blogPostRepository.delete(post);
            return ResponseEntity.ok("Đã xóa bài viết thành công!");
        }).orElse(ResponseEntity.notFound().build());
    }
}