package com.networkroom.skye_portfolio_backend.kernel.mappers;

import java.time.Instant;
import java.util.Date;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.springframework.mail.SimpleMailMessage;

import com.networkroom.skye_portfolio_backend.email.api.dto.ContactRequest;

@Mapper(componentModel = "spring",
		nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
		imports = { Date.class, Instant.class }
)

public interface SimpleMailMessageMapper {

	@Mapping(target = "from", expression = "java(req.email())")
	@Mapping(target = "to", expression = "java(targetEmail)")
	@Mapping(target = "replyTo", expression = "java(req.email())")
	@Mapping(target = "subject", expression = "java(\"📬 New contact message: \" + req.topic())")
	@Mapping(target = "text", expression = "java(buildPlainText(req))")
	@Mapping(target = "cc", ignore = true)
	@Mapping(target = "bcc", ignore = true)
	@Mapping(target = "sentDate", expression = "java(Date.from(Instant.now()))")
	SimpleMailMessage toSimpleMailMessage(ContactRequest req, @Context String targetEmail);

	default String buildPlainText(ContactRequest req) {
		return String.format(
			"New Contact Message\n\n" +
			"👤 Name / Username: %s\n" +
			"📧 Email: %s\n " + 
			"💬 Message:\n%s\n",
			req.username(),
			req.email(),
			req.message()
		);
	}
}
