package com.simudap.facade;

import com.simudap.dto.kis.KisChartDataRequest;
import com.simudap.dto.kis.KisChartDataResponse;
import com.simudap.error.ResourceNotFoundException;
import com.simudap.model.KisToken;
import com.simudap.model.kospi.KospiMaster;
import com.simudap.service.KisApiService;
import com.simudap.service.TokenService;
import com.simudap.service.kospi.KospiMasterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChartFacadeTest {

    @Mock
    private TokenService tokenService;

    @Mock
    private KisApiService kisApiService;

    @Mock
    private KospiMasterService kospiMasterService;

    @InjectMocks
    private ChartFacade chartFacade;

    private String stockCode;
    private String interval;
    private String from;
    private String count;
    private KospiMaster mockKospiMaster;
    private KisToken mockKisToken;
    private KisChartDataResponse mockChartDataResponse;

    @BeforeEach
    void setUp() {
        stockCode = "005930";
        interval = "min:1";
        from = "2025-12-10 17:21:45.123";
        count = "100";

        mockKospiMaster = new KospiMaster(
                stockCode,
                "KR7005930003",
                "삼성전자",
                false
        );

        mockKisToken = new KisToken(
                "mock-rest-api-token",
                "mock-websocket-token",
                LocalDateTime.now().plusHours(24)
        );

        mockChartDataResponse = new KisChartDataResponse(
                stockCode,
                LocalDateTime.now(),
                Collections.emptyList()
        );
    }

    @Test
    @DisplayName("정상적인 차트 조회 요청 시 모든 서비스를 호출하고 차트 데이터를 반환한다")
    void testGetChart_Success() {
        // given
        when(kospiMasterService.findByShortCode(eq(stockCode)))
                .thenReturn(Optional.of(mockKospiMaster));
        when(tokenService.getKisToken())
                .thenReturn(mockKisToken);
        when(kisApiService.getChartData(any(KisChartDataRequest.class), eq(mockKisToken)))
                .thenReturn(mockChartDataResponse);

        // when
        KisChartDataResponse result = chartFacade.getChart(stockCode, interval, from, count);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(mockChartDataResponse);
        assertThat(result.getStockCode()).isEqualTo(stockCode);

        verify(kospiMasterService).findByShortCode(eq(stockCode));
        verify(tokenService).getKisToken();
        verify(kisApiService).getChartData(any(KisChartDataRequest.class), eq(mockKisToken));
    }

    @Test
    @DisplayName("존재하지 않는 stockCode로 조회 시 ResourceNotFoundException이 발생한다")
    void testGetChart_StockCodeNotFound_ThrowsException() {
        // given
        String invalidStockCode = "999999";
        when(kospiMasterService.findByShortCode(eq(invalidStockCode)))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> chartFacade.getChart(invalidStockCode, interval, from, count))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("There is no stock with code " + invalidStockCode);

        verify(kospiMasterService).findByShortCode(eq(invalidStockCode));
        verify(tokenService, never()).getKisToken();
        verify(kisApiService, never()).getChartData(any(), any());
    }

    @Test
    @DisplayName("KospiMasterService에서 종목을 찾은 후 TokenService를 호출한다")
    void testGetChart_CallsTokenServiceAfterFindingStock() {
        // given
        when(kospiMasterService.findByShortCode(eq(stockCode)))
                .thenReturn(Optional.of(mockKospiMaster));
        when(tokenService.getKisToken())
                .thenReturn(mockKisToken);
        when(kisApiService.getChartData(any(KisChartDataRequest.class), eq(mockKisToken)))
                .thenReturn(mockChartDataResponse);

        // when
        chartFacade.getChart(stockCode, interval, from, count);

        // then
        verify(kospiMasterService).findByShortCode(eq(stockCode));
        verify(tokenService).getKisToken();
    }

    @Test
    @DisplayName("KisApiService에 올바른 KisToken과 함께 요청을 전달한다")
    void testGetChart_PassesCorrectTokenToKisApiService() {
        // given
        when(kospiMasterService.findByShortCode(eq(stockCode)))
                .thenReturn(Optional.of(mockKospiMaster));
        when(tokenService.getKisToken())
                .thenReturn(mockKisToken);
        when(kisApiService.getChartData(any(KisChartDataRequest.class), eq(mockKisToken)))
                .thenReturn(mockChartDataResponse);

        // when
        chartFacade.getChart(stockCode, interval, from, count);

        // then
        verify(kisApiService).getChartData(any(KisChartDataRequest.class), eq(mockKisToken));
    }

    @Test
    @DisplayName("일봉 interval로 차트 조회 시 정상적으로 처리된다")
    void testGetChart_WithDayInterval() {
        // given
        String dayInterval = "day:1";
        String dayFrom = "2025-12-10 17:21:45.123";

        when(kospiMasterService.findByShortCode(eq(stockCode)))
                .thenReturn(Optional.of(mockKospiMaster));
        when(tokenService.getKisToken())
                .thenReturn(mockKisToken);
        when(kisApiService.getChartData(any(KisChartDataRequest.class), eq(mockKisToken)))
                .thenReturn(mockChartDataResponse);

        // when
        KisChartDataResponse result = chartFacade.getChart(stockCode, dayInterval, dayFrom, count);

        // then
        assertThat(result).isNotNull();
        verify(kospiMasterService).findByShortCode(eq(stockCode));
        verify(tokenService).getKisToken();
        verify(kisApiService).getChartData(any(KisChartDataRequest.class), eq(mockKisToken));
    }

    @Test
    @DisplayName("3분봉 interval로 차트 조회 시 정상적으로 처리된다")
    void testGetChart_With3MinInterval() {
        // given
        String minInterval = "min:3";

        when(kospiMasterService.findByShortCode(eq(stockCode)))
                .thenReturn(Optional.of(mockKospiMaster));
        when(tokenService.getKisToken())
                .thenReturn(mockKisToken);
        when(kisApiService.getChartData(any(KisChartDataRequest.class), eq(mockKisToken)))
                .thenReturn(mockChartDataResponse);

        // when
        KisChartDataResponse result = chartFacade.getChart(stockCode, minInterval, from, count);

        // then
        assertThat(result).isNotNull();
        verify(kospiMasterService).findByShortCode(eq(stockCode));
        verify(tokenService).getKisToken();
        verify(kisApiService).getChartData(any(KisChartDataRequest.class), eq(mockKisToken));
    }

    @Test
    @DisplayName("다른 종목 코드로 차트 조회 시 정상적으로 처리된다")
    void testGetChart_WithDifferentStockCode() {
        // given
        String differentStockCode = "000660";
        KospiMaster differentMaster = new KospiMaster(
                differentStockCode,
                "KR7000660001",
                "SK하이닉스",
                false
        );

        when(kospiMasterService.findByShortCode(eq(differentStockCode)))
                .thenReturn(Optional.of(differentMaster));
        when(tokenService.getKisToken())
                .thenReturn(mockKisToken);
        when(kisApiService.getChartData(any(KisChartDataRequest.class), eq(mockKisToken)))
                .thenReturn(mockChartDataResponse);

        // when
        KisChartDataResponse result = chartFacade.getChart(differentStockCode, interval, from, count);

        // then
        assertThat(result).isNotNull();
        verify(kospiMasterService).findByShortCode(eq(differentStockCode));
    }

    @Test
    @DisplayName("KospiMasterService에서 반환된 shortCode가 KisChartDataRequest에 전달된다")
    void testGetChart_PassesShortCodeToRequest() {
        // given
        when(kospiMasterService.findByShortCode(eq(stockCode)))
                .thenReturn(Optional.of(mockKospiMaster));
        when(tokenService.getKisToken())
                .thenReturn(mockKisToken);
        when(kisApiService.getChartData(any(KisChartDataRequest.class), eq(mockKisToken)))
                .thenReturn(mockChartDataResponse);

        // when
        chartFacade.getChart(stockCode, interval, from, count);

        // then
        verify(kospiMasterService).findByShortCode(eq(stockCode));
        verify(kisApiService).getChartData(
                argThat(request -> request != null && request.getStockCode().equals(mockKospiMaster.getShortCode())),
                eq(mockKisToken)
        );
    }
}
