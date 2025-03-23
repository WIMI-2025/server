package com.wimi.miro.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class OpenAIConfig {

    @Getter
    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta}")
    private String apiUrl;

    @Getter
    @Value("${gemini.model:gemini-2.0-flash-thinking-exp-01-21}")
    private String model;

    @Bean
    public WebClient geminiWebClient() {
        return WebClient.builder()
                .baseUrl(apiUrl)
                .build();
    }

    public String getGenerateContentEndpoint() {
        return "/models/" + model + ":generateContent?key=" + apiKey;
    }
}