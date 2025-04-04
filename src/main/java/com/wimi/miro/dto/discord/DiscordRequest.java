package com.wimi.miro.dto.discord;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record DiscordRequest(
        String content,
        String username, // 봇 기본값 사용할 예정
        @JsonProperty("avatar_url") String avatarUrl, // 봇 기본값 사용할 예정
        List<Embed> embeds,
        @JsonProperty("allowed_mentions") AllowedMentions allowedMentions
) {

    public record Embed(
            String title,
            String description,
            int color,
            List<Field> fields
    ) {}

    public record Field(
            String name,
            String value,
            boolean inline
    ) {}

    public record AllowedMentions(
            List<String> parse
    ) {}


    public static DiscordRequest create(String content) {
        return new DiscordRequest(content, null, null, null, null);
    }
}

