package com.example.demo.repository;

import com.example.demo.model.BlogPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BlogPostRepository extends JpaRepository<BlogPost, Long> {
    List<BlogPost> findByPublishedTrueOrderByPublishedAtDesc();

    List<BlogPost> findByCategoryAndPublishedTrueOrderByPublishedAtDesc(String category);

    Optional<BlogPost> findBySlugAndPublishedTrue(String slug);

    List<BlogPost> findTop3ByCategoryAndPublishedTrueAndSlugNotOrderByPublishedAtDesc(String category, String slug);

    List<BlogPost> findTop3ByPublishedTrueAndSlugNotOrderByPublishedAtDesc(String slug);
}
