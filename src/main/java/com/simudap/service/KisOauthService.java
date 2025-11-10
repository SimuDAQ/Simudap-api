package com.simudap.service;

import com.simudap.dto.kis_oauth.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class KisOauthService {

    private static final String KIS_DOMAIN = "https://openapi.koreainvestment.com:9443";
    private static final String REST_API_TOKEN_PATH = "/oauth2/tokenP";
    private static final String WEB_SOCKET_TOKEN_PATH = "/oauth2/Approval";

    private final RestTemplate restTemplate;

    @Value("${kis.app-key}")
    private String appKey;
    @Value("${kis.app-secret}")
    private String appSecret;

    public KisTokenInfo getToken() {
        RestApiTokenRequest oauthRequest = new RestApiTokenRequest(appKey, appSecret);
        WebSocketTokenRequest webSocketTokenRequest = new WebSocketTokenRequest(appKey, appSecret);
        RestApiTokenResponse restApiToken = issue(oauthRequest, REST_API_TOKEN_PATH, RestApiTokenResponse.class);
        WebSocketTokenResponse webSocketToken = issue(webSocketTokenRequest, WEB_SOCKET_TOKEN_PATH, WebSocketTokenResponse.class);

        return new KisTokenInfo(restApiToken.token(), webSocketToken.approvalKey(), restApiToken.tokenExpired());
    }

    public <REQ, RES> RES issue(REQ request, String reqPath, Class<RES> responseType) {
        String uri = UriComponentsBuilder
                .fromUriString(KIS_DOMAIN)
                .path(reqPath)
                .toUriString();

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<REQ> entity = new HttpEntity<>(request, headers);

            // 한국투자 증권에 요청해서 토큰 발급 받는 부분
            ResponseEntity<RES> response = restTemplate.postForEntity(uri, entity, responseType);
            return Optional.ofNullable(response.getBody())
                    .orElseThrow(() -> new RuntimeException("Token is null"));
        } catch (Exception e) {
            throw new RuntimeException(e.getCause());
        }
    }
}
