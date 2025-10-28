package com.networkroom.skye_portfolio_backend.platform.mailgun.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@ConfigurationProperties(prefix = "mailgun")
@Configuration
@Data
public class MailgunPropertiesConfig {

    private String apiKey;

    private String domain;

    private String fromEmail;

}

