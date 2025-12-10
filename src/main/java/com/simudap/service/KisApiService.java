package com.simudap.service;

import com.simudap.dto.KisChartDataResponse;
import com.simudap.dto.kis.KisChartDataRequest;
import com.simudap.dto.kis.oauth.*;
import com.simudap.enums.kis.KisRequestHeader;
import com.simudap.error.ExternalApiCallException;
import com.simudap.error.ResourceNotFoundException;
import com.simudap.model.KisToken;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
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
        KisApiTokenResponse apiToken = requestTo(
                HttpMethod.POST,
                buildUrl(REST_API_TOKEN_PATH),
                new HttpEntity<>(oauthRequest, buildDefaultHeaders()),
                KisApiTokenResponse.class
        );
        KisWebSocketTokenResponse webSocketToken = requestTo(
                HttpMethod.POST,
                buildUrl(WEB_SOCKET_TOKEN_PATH),
                new HttpEntity<>(kisWebSocketTokenRequest, buildDefaultHeaders()),
                KisWebSocketTokenResponse.class
        );

        return new KisTokenInfo(apiToken.token(), webSocketToken.approvalKey(), apiToken.tokenExpired());
    }

    // TODO : API 엔드포인트에 따라서 분기처리 필요. Facade layer 에서 분기후 통합
    public KisChartDataResponse getChartData(KisChartDataRequest request, KisToken kisToken) {
        return requestTo(
                HttpMethod.GET,
                buildUrl(request.getPath(), request.getParams()),
                new HttpEntity<>(buildHeaders(request.getTrId())),
                KisChartDataResponse.class
        );
    }

    private <REQ, RES> RES requestTo(HttpMethod method, String uri, HttpEntity<REQ> entity, Class<RES> responseType) {
        try {
            ResponseEntity<RES> response = restTemplate.exchange(uri, method, entity, responseType);

            return Optional.ofNullable(response.getBody())
                    .orElseThrow(() -> new ResourceNotFoundException("Response is null"));
        } catch (Exception e) {
            throw new ExternalApiCallException(e.getMessage());
        }
    }

    private String buildUrl(String path) {
        return UriComponentsBuilder
                .fromUriString(kisDomainUrl)
                .path(path)
                .toUriString();
    }

    private String buildUrl(String path, MultiValueMap<String, String> params) {
        return UriComponentsBuilder
                .fromUriString(kisDomainUrl)
                .path(path)
                .queryParams(params)
                .toUriString();
    }

    private HttpHeaders buildDefaultHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private HttpHeaders buildHeaders(String trId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(KisRequestHeader.APP_KEY.getKey(), appKey);
        headers.set(KisRequestHeader.APP_SECRET.getKey(), appSecret);
        headers.set(KisRequestHeader.TRADE_ID.getKey(), trId);
        headers.set(KisRequestHeader.CUSTOMER_TYPE_P.getKey(), KisRequestHeader.CUSTOMER_TYPE_P.getValue());
        return headers;
    }
}
