package com.simudap.dto.kis;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record KisChartPeriod(
        @JsonProperty("rt_cd")
        String resultCode,

        @JsonProperty("msg_cd")
        String statusCode,

        @JsonProperty("msg1")
        String message,

        @JsonProperty("output1")
        CurrentStockInfo currentStockInfo,

        @JsonProperty("output2")
        List<ChartData> chartDataList
) {
        public record CurrentStockInfo(
                @JsonProperty("prdy_vrss")
                String previousDayChange,    // 전일 대비

                @JsonProperty("prdy_vrss_sign")
                String previousDayChangeSign,    // 전일 대비 부호

                @JsonProperty("prdy_ctrt")
                String previousDayChangeRate,    // 전일 대비율

                @JsonProperty("stck_prdy_clpr")
                String previousDayClosingPrice,    // 주식 전일 종가

                @JsonProperty("acml_vol")
                String accumulatedVolume,    // 누적 거래량

                @JsonProperty("acml_tr_pbmn")
                String accumulatedTradingAmount,    // 누적 거래 대금

                @JsonProperty("hts_kor_isnm")
                String koreanStockName,    // HTS 한글 종목명

                @JsonProperty("stck_prpr")
                String currentPrice,    // 주식 현재가

                @JsonProperty("stck_shrn_iscd")
                String stockCode,    // 주식 단축 종목코드

                @JsonProperty("prdy_vol")
                String previousDayVolume,    // 전일 거래량

                @JsonProperty("stck_mxpr")
                String upperLimitPrice,    // 주식 상한가

                @JsonProperty("stck_llam")
                String lowerLimitPrice,    // 주식 하한가

                @JsonProperty("stck_oprc")
                String openingPrice,    // 주식 시가2

                @JsonProperty("stck_hgpr")
                String highPrice,    // 주식 최고가

                @JsonProperty("stck_lwpr")
                String lowPrice,    // 주식 최저가

                @JsonProperty("stck_prdy_oprc")
                String previousDayOpeningPrice,    // 주식 전일 시가

                @JsonProperty("stck_prdy_hgpr")
                String previousDayHighPrice,    // 주식 전일 최고가

                @JsonProperty("stck_prdy_lwpr")
                String previousDayLowPrice,    // 주식 전일 최저가

                @JsonProperty("askp")
                String askPrice,    // 매도호가

                @JsonProperty("bidp")
                String bidPrice,    // 매수호가

                @JsonProperty("prdy_vrss_vol")
                String previousDayVolumeChange,    // 전일 대비 거래량

                @JsonProperty("vol_tnrt")
                String volumeTurnoverRate,    // 거래량 회전율

                @JsonProperty("stck_fcam")
                String faceValue,    // 주식 액면가

                @JsonProperty("lstn_stcn")
                String listedShares,    // 상장 주수

                @JsonProperty("cpfn")
                String capital,    // 자본금

                @JsonProperty("hts_avls")
                String marketCapitalization,    // HTS 시가총액

                @JsonProperty("per")
                String per,    // PER

                @JsonProperty("eps")
                String eps,    // EPS

                @JsonProperty("pbr")
                String pbr,    // PBR

                @JsonProperty("itewhol_loan_rmnd_ratem")
                String marginLoanBalanceRate    // 전체 융자 잔고 비율
        ) {
        }

        public record ChartData(
                @JsonProperty("stck_bsop_date")
                String businessDate,    // 주식 영업 일자

                @JsonProperty("stck_clpr")
                String closingPrice,    // 주식 종가

                @JsonProperty("stck_oprc")
                String openingPrice,    // 주식 시가2

                @JsonProperty("stck_hgpr")
                String highPrice,    // 주식 최고가

                @JsonProperty("stck_lwpr")
                String lowPrice,    // 주식 최저가

                @JsonProperty("acml_vol")
                String accumulatedVolume,    // 누적 거래량

                @JsonProperty("acml_tr_pbmn")
                String accumulatedTradingAmount,    // 누적 거래 대금

                @JsonProperty("flng_cls_code")
                String lockTypeCode,    // 락 구분 코드

                @JsonProperty("prtt_rate")
                String splitRatio,    // 분할 비율

                @JsonProperty("mod_yn")
                String isModified,    // 변경 여부

                @JsonProperty("prdy_vrss_sign")
                String previousDayChangeSign,    // 전일 대비 부호

                @JsonProperty("prdy_vrss")
                String previousDayChange,    // 전일 대비

                @JsonProperty("revl_issu_reas")
                String revaluationReasonCode    // 재평가사유코드
        ) {
        }
}
