package com.wimi.miro.service;

import com.wimi.miro.dto.discord.DiscordRequest;
import com.wimi.miro.dto.request.RegisterNotificationRequest;
import com.wimi.miro.util.DiscordNotifier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;


@Service
@RequiredArgsConstructor
public class NotifyService {
    private final DiscordNotifier discordNotifier;

    public void notifyUserRegistration(RegisterNotificationRequest request) {
        String message = String.format("""
                👋 WIMI 신규 사용자 등록!
                
                이름: %s
                이메일: %s
                """, request.getName(), request.getEmail());

        // Discord 알림 전송
        discordNotifier.sendNotification(message)
                .subscribe();
    }

}
