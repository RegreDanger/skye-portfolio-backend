package com.networkroom.skye_portfolio_backend.email.app;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.Data;
import reactor.core.publisher.Mono;

@Service
@Validated
@Data
public class BucketService {
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public Mono<Bucket> resolveBucket(String token) {
        return Mono.just(
            buckets.computeIfAbsent(token, k -> Bucket.builder()
                .addLimit(Bandwidth.classic(5, Refill.intervally(5, Duration.ofMinutes(1))))
                    .build()));
    }

}
