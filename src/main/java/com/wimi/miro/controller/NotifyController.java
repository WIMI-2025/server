package com.wimi.miro.controller;

import com.wimi.miro.dto.request.RegisterNotificationRequest;
import com.wimi.miro.service.NotifyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notify")
@RequiredArgsConstructor
public class NotifyController {

    private final NotifyService notifyService;

    @PostMapping("/register-notification")
    public ResponseEntity<String> notifyUserRegistration(
            @RequestBody RegisterNotificationRequest request
    ) {
        notifyService.notifyUserRegistration(request);
        return ResponseEntity.ok("알림이 성공적으로 전송되었습니다.");
    }


}
