package com.simudap.service;

import com.simudap.dto.kis.KisChartDataRequest;
import com.simudap.dto.kis.KisChartDataResponse;
import com.simudap.dto.kis.KisChartMin;
import com.simudap.dto.kis.KisChartPeriod;
import com.simudap.error.ExternalApiCallException;
import com.simudap.model.KisToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KisApiServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private KisApiService kisApiService;

    private KisToken mockKisToken;
    private KisChartDataRequest minIntervalRequest;
    private KisChartDataRequest dayIntervalRequest;
    private KisChartMin mockKisChartMin;
    private KisChartPeriod mockKisChartPeriod;

    @BeforeEach
    void setUp() {
        // ReflectionTestUtils를 사용하여 @Value 필드 주입
        ReflectionTestUtils.setField(kisApiService, "kisDomainUrl", "https://test.api.com");
        ReflectionTestUtils.setField(kisApiService, "appKey", "test-app-key");
        ReflectionTestUtils.setField(kisApiService, "appSecret", "test-app-secret");
        ReflectionTestUtils.setField(kisApiService, "chartMinPastPath", "/chart/min");
        ReflectionTestUtils.setField(kisApiService, "chartPeriodPath", "/chart/period");

        mockKisToken = new KisToken(
                "mock-rest-api-token",
                "mock-websocket-token",
                LocalDateTime.now().plusHours(24)
        );

        // 분봉 요청 (MIN interval)
        minIntervalRequest = KisChartDataRequest.parse(
                "005930",
                "min:1",
                "2025-12-10 17:21:45.123",
                "100"
        );

        // 일봉 요청 (DAY interval)
        dayIntervalRequest = KisChartDataRequest.parse(
                "005930",
                "day:1",
                "2025-12-10 17:21:45.123",
                "100"
        );

        // Mock KisChartMin 응답
        KisChartMin.CurrentStockInfo currentStockInfo = new KisChartMin.CurrentStockInfo(
                "1000", "2", "1.5", "65000", "10000", "650000000", "삼성전자", "66000"
        );

        KisChartMin.ChartData chartData = new KisChartMin.ChartData(
                "20251210", "172145", "66000", "65000", "66500", "65000", "1000", "650000000"
        );

        mockKisChartMin = new KisChartMin(
                "0", "MKG00000", "정상 처리되었습니다.",
                currentStockInfo,
                List.of(chartData)
        );

        // Mock KisChartPeriod 응답
        KisChartPeriod.CurrentStockInfo periodStockInfo = new KisChartPeriod.CurrentStockInfo(
                "1000", "2", "1.5", "65000", "10000", "650000000", "삼성전자", "66000",
                "005930", "9000", "70000", "60000", "65500", "66500", "64500",
                "65000", "67000", "63000", "66500", "66000", "10000", "50.0",
                "500", "1000000", "100000", "1000000000000", "10.5", "6000", "1.2", "5.0"
        );

        KisChartPeriod.ChartData periodChartData = new KisChartPeriod.ChartData(
                "20251210", "66000", "65000", "66500", "65000", "10000", "650000000",
                "00", "0", "N", "2", "1000", ""
        );

        mockKisChartPeriod = new KisChartPeriod(
                "0", "MKG00000", "정상 처리되었습니다.",
                periodStockInfo,
                List.of(periodChartData)
        );
    }

    @Test
    @DisplayName("MIN interval 요청 시 분봉 차트 데이터를 반환한다")
    void testGetChartData_WithMinInterval_ReturnsMinChartData() {
        // given
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(KisChartMin.class)
        )).thenReturn(ResponseEntity.ok(mockKisChartMin));

        // when
        KisChartDataResponse result = kisApiService.getChartData(minIntervalRequest, mockKisToken);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getStockCode()).isEqualTo("005930");
        verify(restTemplate, atLeastOnce()).exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(KisChartMin.class)
        );
    }

    @Test
    @DisplayName("DAY interval 요청 시 기간별 차트 데이터를 반환한다")
    void testGetChartData_WithDayInterval_ReturnsPeriodChartData() {
        // given
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(KisChartPeriod.class)
        )).thenReturn(ResponseEntity.ok(mockKisChartPeriod));

        // when
        KisChartDataResponse result = kisApiService.getChartData(dayIntervalRequest, mockKisToken);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getStockCode()).isEqualTo("005930");
        verify(restTemplate).exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(KisChartPeriod.class)
        );
    }

    @Test
    @DisplayName("분봉 조회 시 KIS API를 정상적으로 호출한다")
    void testGetChartMin_CallsKisApiCorrectly() {
        // given
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(KisChartMin.class)
        )).thenReturn(ResponseEntity.ok(mockKisChartMin));

        // when
        KisChartDataResponse result = kisApiService.getChartData(minIntervalRequest, mockKisToken);

        // then
        assertThat(result).isNotNull();
        verify(restTemplate, atLeastOnce()).exchange(
                contains("/chart/min"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(KisChartMin.class)
        );
    }

    @Test
    @DisplayName("기간별 조회 시 KIS API를 정상적으로 호출한다")
    void testGetChartPeriod_CallsKisApiCorrectly() {
        // given
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(KisChartPeriod.class)
        )).thenReturn(ResponseEntity.ok(mockKisChartPeriod));

        // when
        KisChartDataResponse result = kisApiService.getChartData(dayIntervalRequest, mockKisToken);

        // then
        assertThat(result).isNotNull();
        verify(restTemplate).exchange(
                contains("/chart/period"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(KisChartPeriod.class)
        );
    }

    @Test
    @DisplayName("RestTemplate 응답이 null인 경우 ResourceNotFoundException이 발생한다")
    void testGetChartData_WithNullResponse_ThrowsResourceNotFoundException() {
        // given
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(KisChartPeriod.class)
        )).thenReturn(ResponseEntity.ok(null));

        // when & then
        assertThatThrownBy(() -> kisApiService.getChartData(dayIntervalRequest, mockKisToken))
                .isInstanceOf(ExternalApiCallException.class);
    }

    @Test
    @DisplayName("RestTemplate에서 예외 발생 시 ExternalApiCallException이 발생한다")
    void testGetChartData_WithRestClientException_ThrowsExternalApiCallException() {
        // given
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(KisChartPeriod.class)
        )).thenThrow(new RestClientException("API call failed"));

        // when & then
        assertThatThrownBy(() -> kisApiService.getChartData(dayIntervalRequest, mockKisToken))
                .isInstanceOf(ExternalApiCallException.class);
    }

    @Test
    @DisplayName("분봉 조회 시 예외 발생하면 ExternalApiCallException이 발생한다")
    void testGetChartMin_WithException_ThrowsExternalApiCallException() {
        // given
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(KisChartMin.class)
        )).thenThrow(new RestClientException("Network error"));

        // when & then
        assertThatThrownBy(() -> kisApiService.getChartData(minIntervalRequest, mockKisToken))
                .isInstanceOf(ExternalApiCallException.class)
                .hasMessageContaining("Network error");
    }

    @Test
    @DisplayName("3분봉 요청 시 정상적으로 처리된다")
    void testGetChartData_With3MinInterval() {
        // given
        KisChartDataRequest min3Request = KisChartDataRequest.parse(
                "005930",
                "min:3",
                "2025-12-10 17:21:45.123",
                "100"
        );

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(KisChartMin.class)
        )).thenReturn(ResponseEntity.ok(mockKisChartMin));

        // when
        KisChartDataResponse result = kisApiService.getChartData(min3Request, mockKisToken);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getStockCode()).isEqualTo("005930");
    }

    @Test
    @DisplayName("주봉(WEEK) interval 요청 시 기간별 차트 데이터를 반환한다")
    void testGetChartData_WithWeekInterval() {
        // given
        KisChartDataRequest weekRequest = KisChartDataRequest.parse(
                "005930",
                "week:1",
                "2025-12-10 17:21:45.123",
                "50"
        );

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(KisChartPeriod.class)
        )).thenReturn(ResponseEntity.ok(mockKisChartPeriod));

        // when
        KisChartDataResponse result = kisApiService.getChartData(weekRequest, mockKisToken);

        // then
        assertThat(result).isNotNull();
        verify(restTemplate).exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(KisChartPeriod.class)
        );
    }

    @Test
    @DisplayName("KisToken이 RestTemplate 헤더에 정상적으로 포함된다")
    void testGetChartData_IncludesKisTokenInHeaders() {
        // given
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(KisChartPeriod.class)
        )).thenReturn(ResponseEntity.ok(mockKisChartPeriod));

        // when
        kisApiService.getChartData(dayIntervalRequest, mockKisToken);

        // then
        verify(restTemplate).exchange(
                anyString(),
                eq(HttpMethod.GET),
                argThat(entity -> {
                    String authHeader = entity.getHeaders().getFirst("authorization");
                    return authHeader != null && authHeader.contains("Bearer mock-rest-api-token");
                }),
                eq(KisChartPeriod.class)
        );
    }
}
