package com.simudap.service;

import com.simudap.dto.kis.KisChartDataRequest;
import com.simudap.dto.kis.KisChartDataResponse;
import com.simudap.dto.kis.KisChartMin;
import com.simudap.dto.kis.KisChartPeriod;
import com.simudap.dto.kis.oauth.*;
import com.simudap.enums.ChartInterval;
import com.simudap.enums.kis.KisRequestHeader;
import com.simudap.enums.kis.KisRequestParam;
import com.simudap.error.ExternalApiCallException;
import com.simudap.error.ResourceNotFoundException;
import com.simudap.model.KisToken;
import com.simudap.util.TimeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

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

    public KisChartDataResponse getChartData(KisChartDataRequest request, KisToken kisToken) {
        if (request.getInterval() == ChartInterval.MIN_PAST) {
            return getChartMin(request, kisToken);
        }

        return getChartPeriod(request, kisToken);
    }

    private KisChartDataResponse getChartMin(KisChartDataRequest request, KisToken kisToken) {
        // Kis API 유량제한 : 20/sec
        // 일별 분봉 : 1회 최대 120 개 조회
        // ex) totalMinCount: intervalValue: 3분 * count: 90개 = 270개
        int requiredCandleCnt = request.getIntervalValue() * request.getCount();
        int requiredApiCallCnt = requiredCandleCnt % 120 > 0 ? requiredCandleCnt / 120 + 1 : requiredCandleCnt / 120;

        List<CompletableFuture<KisChartMin>> chartMinFutures = buildSearchFromList(requiredApiCallCnt, request.getFrom())
                .stream()
                .map(searchDateTime -> CompletableFuture.supplyAsync(() ->
                        requestTo(
                                HttpMethod.GET,
                                buildUrl(chartMinPastPath, buildPastParams(request.getStockCode(), searchDateTime)),
                                new HttpEntity<>(buildHeaders(request.getTrId(), kisToken)),
                                KisChartMin.class
                        )
                ))
                .toList();

        List<KisChartMin> allChartMins = chartMinFutures.stream()
                .map(CompletableFuture::join)
                .toList();

        return KisChartDataResponse.of(
                request.getStockCode(),
                request.getInterval(),
                request.getIntervalValue(),
                allChartMins
        );
    }

    private List<LocalDateTime> buildSearchFromList(int requiredApiCallCnt, LocalDateTime currentFrom) {
        List<LocalDateTime> fromList = new ArrayList<>();

        for (int i = 1; i <= requiredApiCallCnt; i++) {
            LocalDateTime adjustedTime = adjustToMarketTime(currentFrom);
            fromList.add(adjustedTime);

            currentFrom = adjustedTime.minusMinutes(120);
        }

        return fromList;
    }

    private LocalDateTime adjustToMarketTime(LocalDateTime dateTime) {
        LocalDateTime marketStart = dateTime.toLocalDate().atTime(9, 0);
        LocalDateTime marketClosing = dateTime.toLocalDate().atTime(15, 30);

        if (dateTime.isBefore(marketStart)) {
            return marketClosing.minusDays(1);
        }

        // 15:30 이후면 15:30으로 조정
        if (dateTime.isAfter(marketClosing)) {
            return marketClosing;
        }

        return dateTime;
    }

    private MultiValueMap<String, String> buildPastParams(String stockCode, LocalDateTime dateTime) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add(KisRequestParam.FID_COND_MRKT_DIV_CODE.name(), "J");
        map.add(KisRequestParam.FID_INPUT_ISCD.name(), stockCode);
        map.add(KisRequestParam.FID_INPUT_HOUR_1.name(), TimeUtils.toTimeString(dateTime));
        map.add(KisRequestParam.FID_INPUT_DATE_1.name(), TimeUtils.toDateString(dateTime));
        map.add(KisRequestParam.FID_PW_DATA_INCU_YN.name(), "Y");
        map.add(KisRequestParam.FID_FAKE_TICK_INCU_YN.name(), "");
        return map;
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

    private record KisChartSimple(
            String path,
            Class<?> responseType
    ) {
        public static KisChartSimple of(String path, Class<?> responseType) {
            return new KisChartSimple(path, responseType);
        }
    }
}
