package com.networkroom.skye_portfolio_backend.platform.filters;

import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.networkroom.skye_portfolio_backend.email.app.BucketService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Component
@Order(2)
@RequiredArgsConstructor
public class RateLimitingFilter implements WebFilter {

	private final BucketService bucketService;

	@Override
	public @NonNull Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
		String clientToken = exchange.getRequest().getHeaders().getFirst("X-CLIENT-KEY"); 

		return bucketService.resolveBucket(clientToken)
			.flatMap(bucket -> {
				if (bucket.tryConsume(1)) {
					return chain.filter(exchange); 
				} else {
					exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
					return exchange.getResponse().setComplete();
				}
			});
	}

	
}
