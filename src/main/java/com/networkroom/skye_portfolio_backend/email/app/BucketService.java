package com.networkroom.skye_portfolio_backend.email.app;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class BucketService {
    private final LoadingCache<String, Bucket> buckets;
    
    @Value("${rate-limit.capacity:10}")
    private int capacity;
    
    @Value("${rate-limit.refill-tokens:10}")
    private int refillTokens;
    
    @Value("${rate-limit.refill-duration-minutes:1}")
    private long refillDurationMinutes;
    
    @Value("${rate-limit.cache-max-size:10000}")
    private long cacheMaxSize;
    
    @Value("${rate-limit.cache-expire-hours:1}")
    private long cacheExpireHours;

    public BucketService() {
        this.buckets = Caffeine.newBuilder()
            .maximumSize(cacheMaxSize)
            .expireAfterAccess(Duration.ofHours(cacheExpireHours))
            .recordStats()
            .build(this::createNewBucket);
        
        log.info("BucketService initialized with capacity={}, refillTokens={}, refillDuration={}min", 
                 capacity, refillTokens, refillDurationMinutes);
    }

    public Mono<Bucket> resolveBucket(String key) {
        if (key == null || key.isBlank()) {
            log.warn("Attempted to resolve bucket with null/blank key");
            return Mono.just(createNewBucket("default"));
        }
        return Mono.fromSupplier(() -> buckets.get(key));
    }

    private Bucket createNewBucket(String key) {
        log.debug("Creating new bucket for key: {}", key);
        return Bucket.builder()
            .addLimit(Bandwidth.classic(
                capacity, 
                Refill.intervally(refillTokens, Duration.ofMinutes(refillDurationMinutes))
            ))
            .build();
    }
    
    public void evictBucket(String key) {
        buckets.invalidate(key);
        log.debug("Evicted bucket for key: {}", key);
    }
    
    public void clearAllBuckets() {
        buckets.invalidateAll();
        log.info("Cleared all rate limit buckets");
    }
    
    public long getCacheSize() {
        return buckets.estimatedSize();
    }
}