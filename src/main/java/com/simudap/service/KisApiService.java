package com.simudap.service;

import com.simudap.dto.kis.KisChartDataRequest;
import com.simudap.dto.kis.oauth.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class KisApiService {

    private static final String REST_API_TOKEN_PATH = "/oauth2/tokenP";
    private static final String WEB_SOCKET_TOKEN_PATH = "/oauth2/Approval";

    private final RestTemplate restTemplate;

    @Value("${kis.domain.url}")
    private String kisDomainUrl;
    @Value("${kis.app-key}")
    private String appKey;
    @Value("${kis.app-secret}")
    private String appSecret;

    public KisTokenInfo getToken() {
        KisApiTokenRequest oauthRequest = new KisApiTokenRequest(appKey, appSecret);
        KisWebSocketTokenRequest kisWebSocketTokenRequest = new KisWebSocketTokenRequest(appKey, appSecret);
        KisApiTokenResponse apiToken =
                requestTo(Header.none(), HttpMethod.POST, buildDefaultUri(REST_API_TOKEN_PATH), oauthRequest, KisApiTokenResponse.class);
        KisWebSocketTokenResponse webSocketToken =
                requestTo(Header.none(), HttpMethod.POST, buildDefaultUri(WEB_SOCKET_TOKEN_PATH), kisWebSocketTokenRequest, KisWebSocketTokenResponse.class);

        return new KisTokenInfo(apiToken.token(), webSocketToken.approvalKey(), apiToken.tokenExpired());
    }

    // TODO : KisChartDataRequest 에 params 생성 로직 및 Kis 요청 로직 만들기
    public void getChartData(KisChartDataRequest request) {
        String uri = UriComponentsBuilder
                .fromHttpUrl(kisDomainUrl)
                .path(request.uriPath())
                .queryParams(request.params())
                .toUriString();
    }

    private String buildDefaultUri(String path) {
        return UriComponentsBuilder
                .fromUriString(kisDomainUrl)
                .path(path)
                .toUriString();
    }

    private <REQ, RES> RES requestTo(HttpHeaders headers, HttpMethod method, String uri, REQ request, Class<RES> responseType) {
        try {
            HttpEntity<REQ> entity = new HttpEntity<>(request, headers);

            ResponseEntity<RES> response;

            if (method == HttpMethod.POST) {
                response = restTemplate.postForEntity(uri, entity, responseType);
            } else if (method == HttpMethod.GET) {
                response = restTemplate.getForEntity(uri, responseType);
            } else {
                throw new IllegalArgumentException("Http Method not supported");
            }

            return Optional.ofNullable(response.getBody())
                    .orElseThrow(() -> new RuntimeException("Token is null"));
        } catch (Exception e) {
            throw new RuntimeException(e.getCause());
        }
    }

    private record Header(
            HttpHeaders headers
    ) {
        public static HttpHeaders none() {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            return headers;
        }
    }
}
