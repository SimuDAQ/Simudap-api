package com.simudap.dto.kis;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record KisChartMin(
        @JsonProperty("rt_cd")
        String resultCode,           // 성공 실패 여부

        @JsonProperty("msg_cd")
        String statusCode,         // 응답코드

        @JsonProperty("msg1")
        String message,            // 응답메세지

        @JsonProperty("output1")
        CurrentStockInfo currentStockInfo,     // 응답상세

        @JsonProperty("output2")
        List<ChartData> chartDataList  // 응답상세
) {
    public record CurrentStockInfo(
            @JsonProperty("prdy_vrss")
            String previousDayChange,         // 전일 대비

            @JsonProperty("prdy_vrss_sign")
            String previousDayChangeSign, // 전일 대비 부호

            @JsonProperty("prdy_ctrt")
            String previousDayChangeRate,         // 전일 대비율

            @JsonProperty("stck_prdy_clpr")
            String previousDayClosingPrice, // 전일대비 종가

            @JsonProperty("acml_vol")
            String accumulatedVolume,           // 누적 거래량

            @JsonProperty("acml_tr_pbmn")
            String accumulatedTradingAmount,    // 누적 거래대금

            @JsonProperty("hts_kor_isnm")
            String koreanStockName,    // 한글 종목명

            @JsonProperty("stck_prpr")
            String currentPrice          // 주식 현재가
    ) {
    }

    public record ChartData(
            @JsonProperty("stck_bsop_date")
            String businessDate, // 주식 영업일자

            @JsonProperty("stck_cntg_hour")
            String tradingTime, // 주식 체결시간

            @JsonProperty("stck_prpr")
            String closingPrice,          // 주식 현재가(분봉 종가)

            @JsonProperty("stck_oprc")
            String openingPrice,          // 주식 시가(첫 번째 체결가)

            @JsonProperty("stck_hgpr")
            String highPrice,          // 주식 최고가

            @JsonProperty("stck_lwpr")
            String lowPrice,          // 주식 최저가

            @JsonProperty("cntg_vol")
            String tradingVolume,            // 체결 거래량

            @JsonProperty("acml_tr_pbmn")
            String accumulatedTradingAmount      // 누적 거래대금
    ) {
    }
}