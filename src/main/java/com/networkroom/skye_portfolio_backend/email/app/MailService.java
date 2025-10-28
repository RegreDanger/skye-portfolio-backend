package com.networkroom.skye_portfolio_backend.email.app;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.mailgun.api.v3.MailgunMessagesApi;
import com.mailgun.model.message.Message;
import com.networkroom.skye_portfolio_backend.email.api.dto.ContactRequest;
import com.networkroom.skye_portfolio_backend.kernel.mappers.MessageMapper;
import com.networkroom.skye_portfolio_backend.platform.mailgun.config.MailgunPropertiesConfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Service
@Validated
@Slf4j
@RequiredArgsConstructor
public class MailService {

    private final MailgunPropertiesConfig props;
	
	private final MailgunMessagesApi sender;
	private final MessageMapper mapper;

	public Mono<Boolean> sendMessage(ContactRequest req) {
        return Mono.fromFuture(() -> {
            log.info("Sending message...");
            Message msg = mapper.toMessage(req, props.getFromEmail());
            return sender.sendMessageAsync(props.getDomain(), msg);
        })
        .map(res -> {
			log.info("Response: {}", res.getMessage());
            if ("Queued. Thank you.".equalsIgnoreCase(res.getMessage())) {
                log.info("✅ Mailgun accepted the message from {}", req.email());
                return true;
            } else {
                log.warn("⚠️ Mailgun response: {}", res.getMessage());
                return false;
            }
        })
        .onErrorResume(e -> {
            log.error("❌ Error sending message", e);
            log.warn("Putting on DB for cron job (TODO)");
            return Mono.just(false);
        });
	}

}
