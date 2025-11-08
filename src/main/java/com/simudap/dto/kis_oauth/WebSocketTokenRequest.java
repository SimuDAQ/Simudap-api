package com.simudap.dto.kis_oauth;

import com.fasterxml.jackson.annotation.JsonProperty;

public record WebSocketTokenRequest(
        @JsonProperty("grant_type")
        String grantType,
        @JsonProperty("appkey")
        String appKey,
        @JsonProperty("secretkey")
        String secretKey
) {
    public WebSocketTokenRequest(String appKey, String appSecret) {
        this("client_credentials", appKey, appSecret);
    }
}
