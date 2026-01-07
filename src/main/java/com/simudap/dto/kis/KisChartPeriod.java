package com.simudap.dto.kis;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record KisChartPeriod(
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

                @Schema(title = "주식 전일 종가")
                @JsonProperty("stck_prdy_clpr")
                String previousDayClosingPrice,

                @Schema(title = "누적 거래량")
                @JsonProperty("acml_vol")
                String accumulatedVolume,

                @Schema(title = "누적 거래 대금")
                @JsonProperty("acml_tr_pbmn")
                String accumulatedTradingAmount,

                @Schema(title = "HTS 한글 종목명")
                @JsonProperty("hts_kor_isnm")
                String koreanStockName,

                @Schema(title = "주식 현재가")
                @JsonProperty("stck_prpr")
                String currentPrice,

                @Schema(title = "주식 단축 종목코드")
                @JsonProperty("stck_shrn_iscd")
                String stockCode,

                @Schema(title = "전일 거래량")
                @JsonProperty("prdy_vol")
                String previousDayVolume,

                @Schema(title = "주식 상한가")
                @JsonProperty("stck_mxpr")
                String upperLimitPrice,

                @Schema(title = "주식 하한가")
                @JsonProperty("stck_llam")
                String lowerLimitPrice,

                @Schema(title = "주식 시가")
                @JsonProperty("stck_oprc")
                String openingPrice,

                @Schema(title = "주식 최고가")
                @JsonProperty("stck_hgpr")
                String highPrice,

                @Schema(title = "주식 최저가")
                @JsonProperty("stck_lwpr")
                String lowPrice,

                @Schema(title = "주식 전일 시가")
                @JsonProperty("stck_prdy_oprc")
                String previousDayOpeningPrice,

                @Schema(title = "주식 전일 최고가")
                @JsonProperty("stck_prdy_hgpr")
                String previousDayHighPrice,

                @Schema(title = "주식 전일 최저가")
                @JsonProperty("stck_prdy_lwpr")
                String previousDayLowPrice,

                @Schema(title = "매도호가")
                @JsonProperty("askp")
                String askPrice,

                @Schema(title = "매수호가")
                @JsonProperty("bidp")
                String bidPrice,

                @Schema(title = "전일 대비 거래량")
                @JsonProperty("prdy_vrss_vol")
                String previousDayVolumeChange,

                @Schema(title = "거래량 회전율")
                @JsonProperty("vol_tnrt")
                String volumeTurnoverRate,

                @Schema(title = "주식 액면가")
                @JsonProperty("stck_fcam")
                String faceValue,

                @Schema(title = "상장 주수")
                @JsonProperty("lstn_stcn")
                String listedShares,

                @Schema(title = "자본금")
                @JsonProperty("cpfn")
                String capital,

                @Schema(title = "HTS 시가총액")
                @JsonProperty("hts_avls")
                String marketCapitalization,

                @Schema(title = "PER")
                @JsonProperty("per")
                String per,

                @Schema(title = "EPS")
                @JsonProperty("eps")
                String eps,

                @Schema(title = "PBR")
                @JsonProperty("pbr")
                String pbr,

                @Schema(title = "전체 융자 잔고 비율")
                @JsonProperty("itewhol_loan_rmnd_ratem")
                String marginLoanBalanceRate
        ) {
        }

        public record ChartData(
                @Schema(title = "주식 영업 일자")
                @JsonProperty("stck_bsop_date")
                String businessDate,

                @Schema(title = "주식 종가")
                @JsonProperty("stck_clpr")
                String closingPrice,

                @Schema(title = "주식 시가")
                @JsonProperty("stck_oprc")
                String openingPrice,

                @Schema(title = "주식 최고가")
                @JsonProperty("stck_hgpr")
                String highPrice,

                @Schema(title = "주식 최저가")
                @JsonProperty("stck_lwpr")
                String lowPrice,

                @Schema(title = "누적 거래량")
                @JsonProperty("acml_vol")
                String accumulatedVolume,

                @Schema(title = "누적 거래 대금")
                @JsonProperty("acml_tr_pbmn")
                String accumulatedTradingAmount,

                @Schema(title = "락 구분 코드")
                @JsonProperty("flng_cls_code")
                String lockTypeCode,

                @Schema(title = "분할 비율")
                @JsonProperty("prtt_rate")
                String splitRatio,

                @Schema(title = "변경 여부")
                @JsonProperty("mod_yn")
                String isModified,

                @Schema(title = "전일 대비 부호")
                @JsonProperty("prdy_vrss_sign")
                String previousDayChangeSign,

                @Schema(title = "전일 대비")
                @JsonProperty("prdy_vrss")
                String previousDayChange,

                @Schema(title = "재평가사유코드")
                @JsonProperty("revl_issu_reas")
                String revaluationReasonCode
        ) {
        }
}
