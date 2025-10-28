package com.networkroom.skye_portfolio_backend.platform.mailgun.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.mailgun.api.v3.MailgunMessagesApi;
import com.mailgun.client.MailgunClient;

import feign.AsyncClient;
import feign.Client;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class AsyncMailgunConfig {

	private final MailgunPropertiesConfig props;

	private static ExecutorService ex = Executors.newVirtualThreadPerTaskExecutor();
	private static AsyncClient.Default<Object> asyncClient = new AsyncClient.Default<>(
		new Client.Default(null, null), ex);

	@Bean
	MailgunMessagesApi mailgunMessagesApi() {
		return MailgunClient.config(props.getApiKey())
			.client(asyncClient)
				.createAsyncApi(MailgunMessagesApi.class);
	}
}
