package com.example.demo.controller;

import com.example.demo.model.BlogPost;
import com.example.demo.repository.BlogPostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/blog-posts")
@CrossOrigin(origins = "*")
public class BlogPostController {

    @Autowired
    private BlogPostRepository blogPostRepository;

    @GetMapping
    public List<BlogPost> getPublishedPosts() {
        return blogPostRepository.findByPublishedTrueOrderByPublishedAtDesc();
    }

    @GetMapping("/category/{category}")
    public List<BlogPost> getPostsByCategory(@PathVariable String category) {
        return blogPostRepository.findByCategoryAndPublishedTrueOrderByPublishedAtDesc(category);
    }

    @GetMapping("/{slug}")
    public ResponseEntity<BlogPost> getPostBySlug(@PathVariable String slug) {
        return blogPostRepository.findBySlugAndPublishedTrue(slug)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{slug}/related")
    public List<BlogPost> getRelatedPosts(@PathVariable String slug) {
        return blogPostRepository.findBySlugAndPublishedTrue(slug)
                .map(post -> {
                    List<BlogPost> related = blogPostRepository
                            .findTop3ByCategoryAndPublishedTrueAndSlugNotOrderByPublishedAtDesc(post.getCategory(), slug);
                    if (!related.isEmpty()) {
                        return related;
                    }
                    return blogPostRepository.findTop3ByPublishedTrueAndSlugNotOrderByPublishedAtDesc(slug);
                })
                .orElseGet(List::of);
    }
}
