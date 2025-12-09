package com.simudap.dto.kis.oauth;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KisWebSocketTokenRequest(
        @JsonProperty("grant_type")
        String grantType,
        @JsonProperty("appkey")
        String appKey,
        @JsonProperty("secretkey")
        String secretKey
) {
    public KisWebSocketTokenRequest(String appKey, String appSecret) {
        this("client_credentials", appKey, appSecret);
    }
}
