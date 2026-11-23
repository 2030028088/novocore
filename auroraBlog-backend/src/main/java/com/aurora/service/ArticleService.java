package com.aurora.service;

import com.aurora.entity.Article;
import com.aurora.exception.ResourceNotFoundException;
import com.aurora.mapper.ArticleMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ArticleService {

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private CacheService cacheService;

    public Page<Article> getArticles(Integer page, Integer size, String category, String search, String tag) {
        Page<Article> pageParam = new Page<>(page, size);
        QueryWrapper<Article> wrapper = new QueryWrapper<>();

        if (StringUtils.hasText(category) && !category.equals("ALL")) {
            wrapper.eq("category", category.toUpperCase());
        }

        if (StringUtils.hasText(search)) {
            String likeSearch = "%" + search.toLowerCase() + "%";
            wrapper.and(w -> w.like("title", likeSearch)
                    .or().like("title_zh", likeSearch)
                    .or().like("summary", likeSearch));
        }

        wrapper.orderByDesc("created_at");

        return articleMapper.selectPage(pageParam, wrapper);
    }

    public Article getArticleById(Long id) {
        Object cached = cacheService.getArticleCache(id);
        if (cached != null && cached instanceof Article) {
            return (Article) cached;
        }

        Article article = articleMapper.selectById(id);
        if (article != null) {
            cacheService.setArticleCache(id, article);
        }
        return article;
    }

    public Article createArticle(Article article) {
        if (article.getReadTime() == null) {
            int contentLength = article.getContent() != null ? article.getContent().length() : 0;
            article.setReadTime(Math.max(1, (int) Math.ceil(contentLength / 200.0)));
        }
        if (article.getAuthor() == null) {
            article.setAuthor("SYS_ADMIN");
        }
        articleMapper.insert(article);
        
        cacheService.deleteAllArticleCache();
        cacheService.refreshStatsCache();
        
        return article;
    }

    public Article updateArticle(Long id, Article article) {
        Article existing = articleMapper.selectById(id);
        if (existing == null) {
            throw new ResourceNotFoundException("Article", id);
        }
        
        article.setId(id);
        if (article.getReadTime() == null && article.getContent() != null) {
            article.setReadTime(Math.max(1, (int) Math.ceil(article.getContent().length() / 200.0)));
        }
        articleMapper.updateById(article);
        
        cacheService.deleteArticleCache(id);
        cacheService.refreshStatsCache();
        
        return articleMapper.selectById(id);
    }

    public boolean deleteArticle(Long id) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new ResourceNotFoundException("Article", id);
        }
        
        boolean deleted = articleMapper.deleteById(id) > 0;
        
        if (deleted) {
            cacheService.deleteArticleCache(id);
            cacheService.refreshStatsCache();
        }
        
        return deleted;
    }

    public List<String> getCategories() {
        return List.of("ALL", "DEVELOPMENT", "DESIGN", "TUTORIAL", "NEWS", "OTHER");
    }

    public Map<String, Object> getStats() {
        Object cached = cacheService.getStatsCache("articles");
        if (cached != null && cached instanceof Map) {
            return (Map<String, Object>) cached;
        }

        QueryWrapper<Article> wrapper = new QueryWrapper<>();
        long totalArticles = articleMapper.selectCount(wrapper);

        wrapper.eq("published", true);
        long publishedArticles = articleMapper.selectCount(wrapper);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalArticles", totalArticles);
        stats.put("totalComments", 0);
        stats.put("totalFavorites", 0);
        stats.put("categories", getCategories());

        cacheService.setStatsCache("articles", stats);
        
        return stats;
    }

    public boolean togglePublish(Long id) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new ResourceNotFoundException("Article", id);
        }
        
        article.setPublished(!article.getPublished());
        boolean updated = articleMapper.updateById(article) > 0;
        
        if (updated) {
            cacheService.deleteArticleCache(id);
            cacheService.refreshStatsCache();
        }
        
        return updated;
    }

    public Map<String, Object> toggleFavorite(Long id) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new ResourceNotFoundException("Article", id);
        }
        
        if (article.getFavorites() == null) {
            article.setFavorites(0);
        }
        article.setFavorites(article.getFavorites() + 1);
        articleMapper.updateById(article);
        
        cacheService.deleteArticleCache(id);
        
        return Map.of(
            "favorites", article.getFavorites(),
            "favorited", true
        );
    }
}