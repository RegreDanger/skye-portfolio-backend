package com.networkroom.skye_portfolio_backend.kernel.mappers;

import java.time.Instant;
import java.util.Date;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;

import com.mailgun.model.message.Message;
import com.networkroom.skye_portfolio_backend.email.api.dto.ContactRequest;

@Mapper(componentModel = "spring",
		nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
		imports = { Date.class, Instant.class }
)

public interface MessageMapper {

	@Mapping(target = "from", expression = "java(req.email())")
	@Mapping(target = "to", expression = "java(targetEmail)")
	@Mapping(target = "replyTo", expression = "java(req.email())")
	@Mapping(target = "subject", expression = "java(\"📬 New contact message: \" + req.topic())")
	@Mapping(target = "text", expression = "java(buildPlainText(req))")

	@Mapping(target = "attachment", ignore = true)
    @Mapping(target = "bcc", ignore = true)
    @Mapping(target = "cc", ignore = true)
    @Mapping(target = "deliveryTime", ignore = true)
    @Mapping(target = "dkim", ignore = true)
    @Mapping(target = "formData", ignore = true)
    @Mapping(target = "headers", ignore = true)
    @Mapping(target = "html", ignore = true)
    @Mapping(target = "inline", ignore = true)
    @Mapping(target = "mailgunVariables", ignore = true)
    @Mapping(target = "myVar", ignore = true)
    @Mapping(target = "recipientVariables", ignore = true)
    @Mapping(target = "renderTemplate", ignore = true)
    @Mapping(target = "requireTls", ignore = true)
    @Mapping(target = "sender", ignore = true)
    @Mapping(target = "skipVerification", ignore = true)
    @Mapping(target = "tag", ignore = true)
    @Mapping(target = "template", ignore = true)
    @Mapping(target = "templateVersion", ignore = true)
    @Mapping(target = "testMode", ignore = true)
    @Mapping(target = "tracking", ignore = true)
    @Mapping(target = "trackingClicks", ignore = true)
    @Mapping(target = "trackingOpens", ignore = true)
	public Message toMessage(ContactRequest req, @Context String targetEmail);

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
