package com.simudap.service;

import com.simudap.dto.kis_oauth.KisOauthRequest;
import com.simudap.dto.kis_oauth.KisOauthResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class KisOauthService {

    private static final String KIS_DOMAIN = "https://openapi.koreainvestment.com:9443";

    private final RestTemplate restTemplate;

    @Value("${kis.app-key}")
    private String appKey;
    @Value("${kis.app-secret}")
    private String appSecret;

    public KisOauthResponse getKisToken() {
        KisOauthRequest oauthRequest = new KisOauthRequest(appKey, appSecret);
        String uri = UriComponentsBuilder
                .fromUriString(KIS_DOMAIN)
                .path("/oauth2/tokenP")
                .toUriString();
        try {
            // 한국투자 증권에 요청해서 토큰 발급 받는 부분
            ResponseEntity<KisOauthResponse> response = restTemplate.postForEntity(uri, oauthRequest, KisOauthResponse.class);
            return Optional.ofNullable(response.getBody())
                    .orElseThrow(() -> new RuntimeException("Token is null"));
        } catch (Exception e) {
            throw new RuntimeException(e.getCause());
        }
    }
}
