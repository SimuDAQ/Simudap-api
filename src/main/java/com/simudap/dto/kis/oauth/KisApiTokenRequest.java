package com.simudap.dto.kis.oauth;


import com.fasterxml.jackson.annotation.JsonProperty;

public record KisApiTokenRequest(
        @JsonProperty("grant_type")
        String grantType,

        @JsonProperty("appkey")
        String appKey,

        @JsonProperty("appsecret")
        String appSecret

) {
    public KisApiTokenRequest(String appKey, String appSecret) {
        this("client_credentials", appKey, appSecret);
    }

}
