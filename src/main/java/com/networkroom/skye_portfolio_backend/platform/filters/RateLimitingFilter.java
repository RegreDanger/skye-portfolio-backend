package com.networkroom.skye_portfolio_backend.platform.filters;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.networkroom.skye_portfolio_backend.email.app.BucketService;

import io.github.bucket4j.ConsumptionProbe;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@Order(2)
@RequiredArgsConstructor
public class RateLimitingFilter implements WebFilter {

	private final BucketService bucketService;
	
	@Value("${rate-limit.enabled:true}")
	private boolean rateLimitEnabled;
	
	@Value("${rate-limit.whitelist-ips:}")
	private List<String> whitelistIps;
	
	@Value("${rate-limit.header-prefix:X-RateLimit}")
	private String headerPrefix;

	@Override
	public @NonNull Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
		if (!rateLimitEnabled) {
			return chain.filter(exchange);
		}
		
		String clientIp = extractClientIp(exchange);
		
		if (isWhitelisted(clientIp)) {
			log.debug("IP {} is whitelisted, skipping rate limit", clientIp);
			return chain.filter(exchange);
		}
		
		log.debug("Checking rate limit for IP: {}", clientIp);
		
		return bucketService.resolveBucket(clientIp)
			.flatMap(bucket -> {
				ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
				
				if (probe.isConsumed()) {
					addRateLimitHeaders(exchange, probe);
					log.debug("Request allowed for IP: {} (remaining: {})", clientIp, probe.getRemainingTokens());
					return chain.filter(exchange);
				} else {
					long waitTimeSeconds = TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill());
					
					exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
					addRateLimitHeaders(exchange, probe);
					exchange.getResponse().getHeaders().add(HttpHeaders.RETRY_AFTER, String.valueOf(waitTimeSeconds));
					
					log.warn("Rate limit exceeded for IP: {} (retry after: {}s)", clientIp, waitTimeSeconds);
					
					return exchange.getResponse().setComplete();
				}
			})
			.onErrorResume(error -> {
				log.error("Error processing rate limit for IP: {}", clientIp, error);
				return chain.filter(exchange);
			});
	}

	private String extractClientIp(ServerWebExchange exchange) {
		String xff = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
		if (xff != null && !xff.isBlank()) {
			String ip = xff.split(",")[0].trim();
			if (isValidIp(ip)) {
				return ip;
			}
		}
		
		String xRealIp = exchange.getRequest().getHeaders().getFirst("X-Real-IP");
		if (xRealIp != null && !xRealIp.isBlank() && isValidIp(xRealIp)) {
			return xRealIp;
		}
		
		InetSocketAddress remote = exchange.getRequest().getRemoteAddress();
		if (remote != null) {
			InetAddress addr = remote.getAddress();
			if (addr != null) {
				return addr.getHostAddress();
			}
			return remote.getHostString();
		}
		
		return "unknown";
	}
	
	private boolean isValidIp(String ip) {
		if (ip == null || ip.isBlank()) {
			return false;
		}
		return ip.matches("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$") || 
		       ip.matches("^(?:[0-9a-fA-F]{0,4}:){2,7}[0-9a-fA-F]{0,4}$");
	}
	
	private boolean isWhitelisted(String ip) {
		return whitelistIps != null && whitelistIps.contains(ip);
	}
	
	private void addRateLimitHeaders(ServerWebExchange exchange, ConsumptionProbe probe) {
		HttpHeaders headers = exchange.getResponse().getHeaders();
		headers.add(headerPrefix + "-Limit", String.valueOf(probe.getRemainingTokens() + 1));
		headers.add(headerPrefix + "-Remaining", String.valueOf(probe.getRemainingTokens()));
		
		if (!probe.isConsumed()) {
			long resetSeconds = TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill());
			headers.add(headerPrefix + "-Reset", String.valueOf(System.currentTimeMillis() / 1000 + resetSeconds));
		}
	}
}