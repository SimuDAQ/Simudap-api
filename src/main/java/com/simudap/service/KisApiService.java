package com.simudap.service;

import com.simudap.dto.kis.KisChartDataRequest;
import com.simudap.dto.kis.KisChartDataResponse;
import com.simudap.dto.kis.KisChartMin;
import com.simudap.dto.kis.KisChartPeriod;
import com.simudap.dto.kis.oauth.*;
import com.simudap.enums.ChartInterval;
import com.simudap.enums.kis.KisRequestHeader;
import com.simudap.error.ExternalApiCallException;
import com.simudap.error.ResourceNotFoundException;
import com.simudap.model.KisToken;
import com.simudap.util.TimeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class KisApiService {

    private static final String REST_API_TOKEN_PATH = "/oauth2/tokenP";
    private static final String WEB_SOCKET_TOKEN_PATH = "/oauth2/Approval";

    private static final int KIS_API_REQUEST_MAX_PER_SEC = 20;
    private static final int DAILY_MIN_REQUEST_MAX = 120;

    private final RestTemplate restTemplate;

    @Value("${kis.domain.url}")
    private String kisDomainUrl;
    @Value("${kis.app-key}")
    private String appKey;
    @Value("${kis.app-secret}")
    private String appSecret;

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

    public KisChartDataResponse getChartData(KisChartDataRequest request, KisToken kisToken) {
        if (request.getInterval() == ChartInterval.MIN) {
            return getChartMin(request, kisToken);
        }

        return getChartPeriod(request, kisToken);
    }

    private KisChartDataResponse getChartMin(KisChartDataRequest request, KisToken kisToken) {
        // Kis API 유량제한 : 20/sec, 일별 분봉 : 1회 최대 120 개 조회 가능
        // 1분봉 이상의 데이터 합산 처리시 데이터 잘리는 걸 방지하기 위해 요청수 보다 + 1 하여 Kis 요청 후 server 단에서 잘라서 제공
        int requiredCandleCnt = request.getIntervalValue() * (request.getCount() + 1);
        int requiredApiCallCnt = requiredCandleCnt < DAILY_MIN_REQUEST_MAX ? 1 : requiredCandleCnt / DAILY_MIN_REQUEST_MAX;
        LocalDateTime nextFrom = request.getFrom();
        List<KisChartMin> charts = new ArrayList<>();

        try {
            while (requiredApiCallCnt > 0) {
                // KIS API call 유량 제한 1초 20회
                if (requiredApiCallCnt % KIS_API_REQUEST_MAX_PER_SEC == 0) {
                    Thread.sleep(1000);
                }

                KisChartMin response = requestTo(
                        HttpMethod.GET,
                        buildUrl(chartMinPastPath, request.buildMinParams(nextFrom)),
                        new HttpEntity<>(buildHeaders(request.getTrId(), kisToken)),
                        KisChartMin.class
                );

                KisChartMin.ChartData last = response.chartDataList().getLast();
                nextFrom = TimeUtils.toLocalDateTime2(last.businessDate() + last.tradingTime());
                charts.add(response);

                requiredApiCallCnt--;
            }
        } catch (Exception e) {
            throw new ExternalApiCallException(e.getMessage());
        }

        return KisChartDataResponse.of(request, charts);
    }

    public KisChartDataResponse getChartPeriod(KisChartDataRequest request, KisToken kisToken) {
        KisChartPeriod response = requestTo(
                HttpMethod.GET,
                buildUrl(chartPeriodPath, request.getParams()),
                new HttpEntity<>(buildHeaders(request.getTrId(), kisToken)),
                KisChartPeriod.class
        );

        return KisChartDataResponse.of(request.getStockCode(), request.getInterval(), request.getIntervalValue(), response);
    }

    private <REQ, RES> RES requestTo(HttpMethod method, String uri, HttpEntity<REQ> entity, Class<RES> responseType) {
        try {
            ResponseEntity<RES> response = restTemplate.exchange(uri, method, entity, responseType);

            return Optional.ofNullable(response.getBody())
                    .orElseThrow(() -> new ResourceNotFoundException("Response is null"));
        } catch (Exception e) {
            log.warn(e.getMessage());
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
}
