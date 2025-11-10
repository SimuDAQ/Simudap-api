package com.simudap.dto.kis_oauth;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record KisTokenInfo(
        String restApiToken,
        String webSocketToken,
        String tokenExpired
) {
    public LocalDateTime getTokenExpired() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.parse(tokenExpired, formatter);
    }
}
