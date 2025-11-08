package com.simudap.dto.kis_oauth;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record KisOauthResponse(
        @JsonProperty("access_token")
        String token,

        @JsonProperty("token_type")
        String tokenType,

        @JsonProperty("expires_in")
        Long expiresIn,

        @JsonProperty("access_token_token_expired")
        String tokenExpired
) {
    public LocalDateTime getTokenExpired() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.parse(tokenExpired, formatter);
    }
}
