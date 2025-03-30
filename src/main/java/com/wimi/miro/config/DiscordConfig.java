package com.wimi.miro.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class DiscordConfig {

    @Value("${discord.webhook.error}")
    private String errorWebhookUrl;

    @Bean
    public WebClient DiscordErrorWebhookClient() {
        return WebClient.builder()
                .baseUrl(errorWebhookUrl)
                .build();
    }

}
