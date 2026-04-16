package com.simudap.controller;

import com.simudap.dto.kis.KisChartDataResponse;
import com.simudap.facade.ChartFacade;
import com.simudap.util.response.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChartControllerTest {

    @Mock
    private ChartFacade chartFacade;

    @InjectMocks
    private ChartController chartController;

    private KisChartDataResponse mockChartDataResponse;
    private String stockCode;
    private String interval;
    private LocalDateTime from;
    private String count;

    @BeforeEach
    void setUp() {
        stockCode = "005930";
        interval = "min:1";
        from = LocalDateTime.of(2025, 12, 10, 17, 21, 45, 123_000_000);
        count = "100";

        mockChartDataResponse = new KisChartDataResponse(
                stockCode,
                LocalDateTime.now(),
                Collections.emptyList()
        );
    }

    @Test
    @DisplayName("정상적인 차트 조회 요청 시 ChartFacade를 호출하고 ApiResponse를 반환한다")
    void testGetChart_Success() {
        // given
        when(chartFacade.getChart(eq(stockCode), eq(interval), eq(from), eq(count)))
                .thenReturn(mockChartDataResponse);

        // when
        ResponseEntity<ApiResponse<KisChartDataResponse>> result =
                chartController.getChart(stockCode, interval, from, count);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().data()).isEqualTo(mockChartDataResponse);
        assertThat(result.getBody().error()).isNull();

        verify(chartFacade).getChart(eq(stockCode), eq(interval), eq(from), eq(count));
    }

    @Test
    @DisplayName("ChartFacade에서 반환된 응답이 ApiResponse로 올바르게 감싸진다")
    void testGetChart_ResponseWrappedCorrectly() {
        // given
        when(chartFacade.getChart(any(), any(), any(), any()))
                .thenReturn(mockChartDataResponse);

        // when
        ResponseEntity<ApiResponse<KisChartDataResponse>> result =
                chartController.getChart(stockCode, interval, from, count);

        // then
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().data()).isNotNull();
        assertThat(result.getBody().data().getStockCode()).isEqualTo(stockCode);
        assertThat(result.getBody().data().getCandles()).isNotNull();
        assertThat(result.getBody().data().getNextDateTime()).isNotNull();
    }

    @Test
    @DisplayName("분봉(MIN) interval로 차트 조회 시 정상적으로 처리된다")
    void testGetChart_WithMinInterval() {
        // given
        String minInterval = "min:10";
        when(chartFacade.getChart(eq(stockCode), eq(minInterval), eq(from), eq(count)))
                .thenReturn(mockChartDataResponse);

        // when
        ResponseEntity<ApiResponse<KisChartDataResponse>> result =
                chartController.getChart(stockCode, minInterval, from, count);

        // then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(chartFacade).getChart(eq(stockCode), eq(minInterval), eq(from), eq(count));
    }

    @Test
    @DisplayName("일봉(DAY) interval로 차트 조회 시 정상적으로 처리된다")
    void testGetChart_WithDayInterval() {
        // given
        String dayInterval = "day:1";
        when(chartFacade.getChart(eq(stockCode), eq(dayInterval), eq(from), eq(count)))
                .thenReturn(mockChartDataResponse);

        // when
        ResponseEntity<ApiResponse<KisChartDataResponse>> result =
                chartController.getChart(stockCode, dayInterval, from, count);

        // then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(chartFacade).getChart(eq(stockCode), eq(dayInterval), eq(from), eq(count));
    }

    @Test
    @DisplayName("다양한 count 값으로 차트 조회 시 정상적으로 처리된다")
    void testGetChart_WithDifferentCountValues() {
        // given
        String largeCount = "100";
        when(chartFacade.getChart(eq(stockCode), eq(interval), eq(from), eq(largeCount)))
                .thenReturn(mockChartDataResponse);

        // when
        ResponseEntity<ApiResponse<KisChartDataResponse>> result =
                chartController.getChart(stockCode, interval, from, largeCount);

        // then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(chartFacade).getChart(eq(stockCode), eq(interval), eq(from), eq(largeCount));
    }
}
