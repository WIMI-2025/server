package com.wimi.miro.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RegisterNotificationRequest {
    private String name;
    private String email;
}
