package com.simudap.dto.kis_oauth;


import com.fasterxml.jackson.annotation.JsonProperty;

public record RestApiTokenRequest(
        @JsonProperty("grant_type")
        String grantType,

        @JsonProperty("appkey")
        String appKey,

        @JsonProperty("appsecret")
        String appSecret

) {
    public RestApiTokenRequest(String appKey, String appSecret) {
        this("client_credentials", appKey, appSecret);
    }

}
