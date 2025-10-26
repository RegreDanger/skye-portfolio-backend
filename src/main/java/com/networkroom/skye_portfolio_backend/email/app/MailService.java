package com.networkroom.skye_portfolio_backend.email.app;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.networkroom.skye_portfolio_backend.email.api.dto.ContactRequest;
import com.networkroom.skye_portfolio_backend.kernel.mappers.SimpleMailMessageMapper;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@Validated
@Data
@Slf4j
public class MailService {

	@Value("${ICLOUD_SKYE_EMAIL}")
	private String ICLOUD_SKYE_EMAIL;
	
	private final JavaMailSender sender;
	private final SimpleMailMessageMapper mapper;

	public Mono<Boolean> sendMessage(ContactRequest req) {
		return Mono.fromCallable(() -> {
			log.info("Sending message...");
			sender.send(mapper.toSimpleMailMessage(req, ICLOUD_SKYE_EMAIL));
			log.info("✅ Email sent from {} from the user: {}", req.email(), req.username());
			return true;
		})
		.subscribeOn(Schedulers.boundedElastic())
		.onErrorResume(e -> {
			log.error("❌ There was an error sending the message", e);
			log.warn("Putting on DB for cron job (TODO)");
			return Mono.just(false);
		});
	}

}
