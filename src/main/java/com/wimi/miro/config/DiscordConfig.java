package com.wimi.miro.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class DiscordConfig {

    @Value("${discord.webhook.error}")
    private String errorWebhookUrl;

    @Value("${discord.webhook.register}")
    private String registerWebhookUrl;



    @Bean
    public WebClient DiscordErrorWebhookClient() {
        return WebClient.builder()
                .baseUrl(errorWebhookUrl)
                .build();
    }

    @Bean
    public WebClient DiscordRegisterWebhookClient() {
        return WebClient.builder()
                .baseUrl(registerWebhookUrl)
                .build();
    }

}
