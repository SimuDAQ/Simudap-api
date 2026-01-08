package com.simudap.dto.kis;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record KisChartMin(
        @Schema(title = "성공 실패 여부")
        @JsonProperty("rt_cd")
        String resultCode,

        @Schema(title = "응답코드")
        @JsonProperty("msg_cd")
        String statusCode,

        @Schema(title = "응답메세지")
        @JsonProperty("msg1")
        String message,

        @Schema(title = "현재 주식 정보")
        @JsonProperty("output1")
        CurrentStockInfo currentStockInfo,

        @Schema(title = "차트 데이터 리스트")
        @JsonProperty("output2")
        List<ChartData> chartDataList
) {
    public record CurrentStockInfo(
            @Schema(title = "전일 대비")
            @JsonProperty("prdy_vrss")
            String previousDayChange,

            @Schema(title = "전일 대비 부호")
            @JsonProperty("prdy_vrss_sign")
            String previousDayChangeSign,

            @Schema(title = "전일 대비율")
            @JsonProperty("prdy_ctrt")
            String previousDayChangeRate,

            @Schema(title = "전일 종가")
            @JsonProperty("stck_prdy_clpr")
            String previousDayClosingPrice,

            @Schema(title = "누적 거래량")
            @JsonProperty("acml_vol")
            String accumulatedVolume,

            @Schema(title = "누적 거래대금")
            @JsonProperty("acml_tr_pbmn")
            String accumulatedTradingAmount,

            @Schema(title = "한글 종목명")
            @JsonProperty("hts_kor_isnm")
            String koreanStockName,

            @Schema(title = "주식 현재가")
            @JsonProperty("stck_prpr")
            String currentPrice
    ) {
    }

    public record ChartData(
            @Schema(title = "주식 영업일자")
            @JsonProperty("stck_bsop_date")
            String businessDate,

            @Schema(title = "주식 체결시간")
            @JsonProperty("stck_cntg_hour")
            String tradingTime,

            @Schema(title = "주식 현재가(분봉 종가)")
            @JsonProperty("stck_prpr")
            String closingPrice,

            @Schema(title = "주식 시가(첫 번째 체결가)")
            @JsonProperty("stck_oprc")
            String openingPrice,

            @Schema(title = "주식 최고가")
            @JsonProperty("stck_hgpr")
            String highPrice,

            @Schema(title = "주식 최저가")
            @JsonProperty("stck_lwpr")
            String lowPrice,

            @Schema(title = "체결 거래량")
            @JsonProperty("cntg_vol")
            String tradingVolume,

            @Schema(title = "누적 거래대금")
            @JsonProperty("acml_tr_pbmn")
            String accumulatedTradingAmount
    ) {
    }
}