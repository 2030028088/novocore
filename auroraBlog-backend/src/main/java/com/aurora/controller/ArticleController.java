package com.aurora.controller;

import com.aurora.entity.Article;
import com.aurora.service.ArticleService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/articles")
@CrossOrigin(origins = "*")
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getArticles(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "6") Integer perPage,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String tag) {

        Page<Article> pageResult = articleService.getArticles(page, perPage, category, search, tag);

        return ResponseEntity.ok(Map.of(
            "items", pageResult.getRecords(),
            "total", pageResult.getTotal(),
            "page", pageResult.getCurrent(),
            "perPage", pageResult.getSize(),
            "totalPages", pageResult.getPages()
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Article> getArticle(@PathVariable Long id) {
        Article article = articleService.getArticleById(id);
        if (article == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(article);
    }

    @PostMapping
    public ResponseEntity<Article> createArticle(@RequestBody Article article) {
        Article created = articleService.createArticle(article);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Article> updateArticle(@PathVariable Long id, @RequestBody Article article) {
        Article updated = articleService.updateArticle(id, article);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Boolean>> deleteArticle(@PathVariable Long id) {
        boolean deleted = articleService.deleteArticle(id);
        return ResponseEntity.ok(Map.of("success", deleted));
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories() {
        return ResponseEntity.ok(articleService.getCategories());
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(articleService.getStats());
    }

    @PatchMapping("/{id}/toggle-publish")
    public ResponseEntity<Map<String, Boolean>> togglePublish(@PathVariable Long id) {
        boolean newStatus = articleService.togglePublish(id);
        return ResponseEntity.ok(Map.of("published", newStatus));
    }

    @PostMapping("/{id}/favorite")
    public ResponseEntity<Map<String, Object>> toggleFavorite(@PathVariable Long id) {
        Map<String, Object> result = articleService.toggleFavorite(id);
        return ResponseEntity.ok(result);
    }
}