package com.wimi.miro.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wimi.miro.config.DiscordConfig;
import com.wimi.miro.dto.discord.DiscordRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.io.PrintWriter;
import java.io.StringWriter;

@Slf4j
@Component
@RequiredArgsConstructor
public class DiscordNotifier {
    private final DiscordConfig discordConfig;

    private final ObjectMapper objectMapper;

    public Mono<String> sendErrorNotification(String errorMessage, Throwable ex) {
        // 스택트레이스를 문자열로 변환
        String stackTrace = "";
        if (ex != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            stackTrace = sw.toString();

            // 디스코드 메시지 길이 제한(2000자)을 고려해 필요시 잘라내기
            if (stackTrace.length() > 1500) {
                stackTrace = stackTrace.substring(0, 1500) + "...";
            }
        }

        // 메시지 내용 구성
        String content = ex != null
                ? String.format("서버 에러 발생: %s\n\n```java\n%s\n```", errorMessage, stackTrace)
                : errorMessage;

        DiscordRequest request = DiscordRequest.create(content);

        String jsonPayload;
        try {
            jsonPayload = objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            return Mono.error(e);
        }

        return discordConfig.DiscordErrorWebhookClient().post()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(jsonPayload)
                .retrieve()
                .bodyToMono(String.class);
    }


    public Mono<String> sendNotification(String message) {
        DiscordRequest request = DiscordRequest.create(message);

        String jsonPayload;
        try {
            jsonPayload = objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            log.error("Discord Norifier JSON Parsing Error", e);
            return Mono.error(e);
        }

        return discordConfig.DiscordRegisterWebhookClient().post()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(jsonPayload)
                .retrieve()
                .bodyToMono(String.class)
                .doOnSuccess(response -> log.info("Discord Notify Success: {}", response))
                .doOnError(error -> log.error("Discord Notify Fail", error));
    }


}
