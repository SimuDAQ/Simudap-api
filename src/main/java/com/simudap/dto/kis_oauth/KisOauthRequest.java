package com.simudap.dto.kis_oauth;

public record KisOauthRequest(
        String grant_type,
        String appkey,
        String appsecret
) {
    public KisOauthRequest(String appKey, String appSecret) {
        this("client_credentials", appKey, appSecret);
    }

}
