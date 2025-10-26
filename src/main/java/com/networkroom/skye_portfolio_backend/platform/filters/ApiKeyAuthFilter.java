package com.networkroom.skye_portfolio_backend.platform.filters;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest; // <-- Importante para seguridad

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@Order(1)
@Slf4j
public class ApiKeyAuthFilter implements WebFilter {

	@Value("${API_KEY_BACKEND}")
	private String API_KEY;

	@Override
	public @NonNull Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
		
		String path = exchange.getRequest().getPath().toString();
		String ip = "unknown";
		InetSocketAddress remote = exchange.getRequest().getRemoteAddress();
		if (remote != null) {
			InetAddress addr = remote.getAddress();
			if (addr != null) {
				ip = addr.getHostAddress();
			} else {
				ip = remote.getHostString();
			}
		} else {
			String xff = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
			if (xff != null && !xff.isBlank()) {
				ip = xff.split(",")[0].trim();
			}
		}

		String clientToken = exchange.getRequest().getHeaders().getFirst("X-CLIENT-KEY");

		if (clientToken == null) {
			log.warn("⛔ API Key missing [X-CLIENT-KEY]. Denying request. Path: {}, IP: {}", path, ip);
			exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
			return exchange.getResponse().setComplete(); 
		}

		boolean isValid = MessageDigest.isEqual(
			clientToken.getBytes(StandardCharsets.UTF_8), 
			API_KEY.getBytes(StandardCharsets.UTF_8)
		);

		if (!isValid) {
			log.warn("⛔ Invalid API Key provided. Denying request. Path: {}, IP: {}", path, ip);
			exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
			return exchange.getResponse().setComplete(); 
		}

		log.debug("✅ API Key validated. Allowing request. Path: {}, IP: {}", path, ip);
		return chain.filter(exchange);
	}
}