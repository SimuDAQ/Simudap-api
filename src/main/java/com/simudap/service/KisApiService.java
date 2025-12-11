package com.simudap.service;

import com.simudap.dto.kis.KisChartDataRequest;
import com.simudap.dto.kis.KisChartDataResponse;
import com.simudap.dto.kis.KisChartMinPast;
import com.simudap.dto.kis.KisChartMinToday;
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

    // 당일 분봉 조회(당일 장 운영시간의 분봉 조회는 이게 더 빠름)
    @Value("${kis.path.chart-min-today}")
    private String chartMinTodayPath;
    // 일별 분봉 조회
    @Value("${kis.path.chart-min-past}")
    private String chartMinPastPath;
    // 기간(일/주/월/년) 별 조회
    @Value("${kis.path.chart-period}")
    private String chartPeriodPath;

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

    // TODO : minPast, period response 통합 로직 추가 필요
    public KisChartDataResponse getChartData(KisChartDataRequest request, KisToken kisToken) {
        KisChartRequestSimple simple = switch (request.getInterval()) {
            case MIN_TODAY -> new KisChartRequestSimple(chartMinTodayPath, KisChartMinToday.class);
            case MIN_PAST -> new KisChartRequestSimple(chartMinPastPath, KisChartMinPast.class);
            case DAY, WEEK, MONTH, YEAR -> new KisChartRequestSimple(chartPeriodPath, KisChartDataResponse.class);
        };

        Object response = requestTo(
                HttpMethod.GET,
                buildUrl(simple.path(), request.getParams()),
                new HttpEntity<>(buildHeaders(request.getTrId(), kisToken)),
                simple.responseType()
        );

        if (response instanceof KisChartMinToday today) {
            return KisChartDataResponse.of(request.getStockCode(), request.getIntervalValue(), today);
        }

        return null;
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

    private HttpHeaders buildHeaders(String trId, KisToken kisToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(KisRequestHeader.APP_KEY.getKey(), appKey);
        headers.set(KisRequestHeader.APP_SECRET.getKey(), appSecret);
        headers.set(KisRequestHeader.AUTHORIZATION.getKey(), "Bearer " + kisToken.getRestApiToken());
        headers.set(KisRequestHeader.TRADE_ID.getKey(), trId);
        headers.set(KisRequestHeader.CUSTOMER_TYPE_P.getKey(), KisRequestHeader.CUSTOMER_TYPE_P.getValue());
        return headers;
    }

    private record KisChartRequestSimple(
            String path,
            Class<?> responseType
    ) {
        public static KisChartRequestSimple of(String path, Class<?> responseType) {
            return new KisChartRequestSimple(path, responseType);
        }
    }
}
