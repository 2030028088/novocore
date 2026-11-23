package com.aurora.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class CacheService {
    
    private static final Logger logger = LoggerFactory.getLogger(CacheService.class);
    
    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;
    
    private static final String ARTICLE_CACHE_PREFIX = "article:";
    private static final String PROJECT_CACHE_PREFIX = "project:";
    private static final String STATS_CACHE_PREFIX = "stats:";
    private static final long DEFAULT_EXPIRE_TIME = 300;
    
    private final Map<String, Object> memoryCache = new ConcurrentHashMap<>();
    private final Map<String, Long> memoryCacheExpiry = new ConcurrentHashMap<>();
    
    private boolean useMemoryFallback = false;
    
    private void checkRedisConnection() {
        if (redisTemplate == null) {
            useMemoryFallback = true;
            logger.warn("Redis not configured, using memory cache fallback");
            return;
        }
        try {
            redisTemplate.getConnectionFactory().getConnection().ping();
            useMemoryFallback = false;
        } catch (Exception e) {
            useMemoryFallback = true;
            logger.warn("Redis connection failed, falling back to memory cache: {}", e.getMessage());
        }
    }
    
    public void set(String key, Object value) {
        set(key, value, DEFAULT_EXPIRE_TIME);
    }
    
    public void set(String key, Object value, long expireSeconds) {
        if (!useMemoryFallback) {
            checkRedisConnection();
        }
        
        if (useMemoryFallback || redisTemplate == null) {
            memoryCache.put(key, value);
            memoryCacheExpiry.put(key, System.currentTimeMillis() + expireSeconds * 1000);
            return;
        }
        
        try {
            redisTemplate.opsForValue().set(key, value, expireSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.warn("Redis set failed, using memory cache: {}", e.getMessage());
            memoryCache.put(key, value);
            memoryCacheExpiry.put(key, System.currentTimeMillis() + expireSeconds * 1000);
        }
    }
    
    public Object get(String key) {
        if (!useMemoryFallback) {
            checkRedisConnection();
        }
        
        if (useMemoryFallback || redisTemplate == null) {
            Long expiry = memoryCacheExpiry.get(key);
            if (expiry != null && System.currentTimeMillis() > expiry) {
                memoryCache.remove(key);
                memoryCacheExpiry.remove(key);
                return null;
            }
            return memoryCache.get(key);
        }
        
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            logger.warn("Redis get failed, using memory cache: {}", e.getMessage());
            Long expiry = memoryCacheExpiry.get(key);
            if (expiry != null && System.currentTimeMillis() > expiry) {
                memoryCache.remove(key);
                memoryCacheExpiry.remove(key);
                return null;
            }
            return memoryCache.get(key);
        }
    }
    
    public boolean exists(String key) {
        if (!useMemoryFallback) {
            checkRedisConnection();
        }
        
        if (useMemoryFallback || redisTemplate == null) {
            Long expiry = memoryCacheExpiry.get(key);
            if (expiry != null && System.currentTimeMillis() > expiry) {
                memoryCache.remove(key);
                memoryCacheExpiry.remove(key);
                return false;
            }
            return memoryCache.containsKey(key);
        }
        
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            logger.warn("Redis exists failed: {}", e.getMessage());
            Long expiry = memoryCacheExpiry.get(key);
            if (expiry != null && System.currentTimeMillis() > expiry) {
                memoryCache.remove(key);
                memoryCacheExpiry.remove(key);
                return false;
            }
            return memoryCache.containsKey(key);
        }
    }
    
    public void delete(String key) {
        if (!useMemoryFallback) {
            checkRedisConnection();
        }
        
        memoryCache.remove(key);
        memoryCacheExpiry.remove(key);
        
        if (!useMemoryFallback && redisTemplate != null) {
            try {
                redisTemplate.delete(key);
            } catch (Exception e) {
                logger.warn("Redis delete failed: {}", e.getMessage());
            }
        }
    }
    
    public void deleteByPattern(String pattern) {
        memoryCache.keySet().removeIf(k -> k.startsWith(pattern.replace("*", "")));
        memoryCacheExpiry.keySet().removeIf(k -> k.startsWith(pattern.replace("*", "")));
        
        if (!useMemoryFallback && redisTemplate != null) {
            try {
                var keys = redisTemplate.keys(pattern);
                if (keys != null && !keys.isEmpty()) {
                    redisTemplate.delete(keys);
                }
            } catch (Exception e) {
                logger.warn("Redis deleteByPattern failed: {}", e.getMessage());
            }
        }
    }
    
    public void setArticleCache(Long id, Object article) {
        String key = ARTICLE_CACHE_PREFIX + id;
        set(key, article, 600);
    }
    
    public Object getArticleCache(Long id) {
        String key = ARTICLE_CACHE_PREFIX + id;
        return get(key);
    }
    
    public void deleteArticleCache(Long id) {
        String key = ARTICLE_CACHE_PREFIX + id;
        delete(key);
    }
    
    public void deleteAllArticleCache() {
        deleteByPattern(ARTICLE_CACHE_PREFIX + "*");
    }
    
    public void setProjectCache(Long id, Object project) {
        String key = PROJECT_CACHE_PREFIX + id;
        set(key, project, 600);
    }
    
    public Object getProjectCache(Long id) {
        String key = PROJECT_CACHE_PREFIX + id;
        return get(key);
    }
    
    public void deleteProjectCache(Long id) {
        String key = PROJECT_CACHE_PREFIX + id;
        delete(key);
    }
    
    public void deleteAllProjectCache() {
        deleteByPattern(PROJECT_CACHE_PREFIX + "*");
    }
    
    public void setStatsCache(String statsType, Object stats) {
        String key = STATS_CACHE_PREFIX + statsType;
        set(key, stats, 300);
    }
    
    public Object getStatsCache(String statsType) {
        String key = STATS_CACHE_PREFIX + statsType;
        return get(key);
    }
    
    public void deleteStatsCache(String statsType) {
        String key = STATS_CACHE_PREFIX + statsType;
        delete(key);
    }
    
    public void invalidateCache(String cacheType) {
        switch (cacheType.toLowerCase()) {
            case "article":
                deleteAllArticleCache();
                break;
            case "project":
                deleteAllProjectCache();
                break;
            case "stats":
                deleteByPattern(STATS_CACHE_PREFIX + "*");
                break;
            case "all":
                memoryCache.clear();
                memoryCacheExpiry.clear();
                if (!useMemoryFallback && redisTemplate != null) {
                    try {
                        deleteByPattern("*");
                    } catch (Exception e) {
                        logger.warn("Redis invalidate all failed: {}", e.getMessage());
                    }
                }
                break;
            default:
                break;
        }
    }
    
    public void refreshStatsCache() {
        deleteByPattern(STATS_CACHE_PREFIX + "*");
    }
}