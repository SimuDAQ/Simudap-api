package com.simudap.dto.kis;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record KisChartMinToday(
        @JsonProperty("rt_cd")
        String rtCd,           // 성공 실패 여부

        @JsonProperty("msg_cd")
        String msgCd,         // 응답코드

        @JsonProperty("msg1")
        String msg1,            // 응답메세지

        @JsonProperty("output1")
        Output1 output1,     // 응답상세

        @JsonProperty("output2")
        List<Output2> output2  // 응답상세
) {
    public record Output1(
            @JsonProperty("prdy_vrss")
            String prdyVrss,         // 전일 대비

            @JsonProperty("prdy_vrss_sign")
            String prdyVrssSign, // 전일 대비 부호

            @JsonProperty("prdy_ctrt")
            String prdyCtrt,         // 전일 대비율

            @JsonProperty("stck_prdy_clpr")
            String stckPrdyClpr, // 전일대비 종가

            @JsonProperty("acml_vol")
            String acmlVol,           // 누적 거래량

            @JsonProperty("acml_tr_pbmn")
            String acmlTrPbmn,    // 누적 거래대금

            @JsonProperty("hts_kor_isnm")
            String htsKorIsnm,    // 한글 종목명

            @JsonProperty("stck_prpr")
            String stckPrpr          // 주식 현재가
    ) {
    }

    public record Output2(
            @JsonProperty("stck_bsop_date")
            String stckBsopDate, // 주식 영업일자

            @JsonProperty("stck_cntg_hour")
            String stckCntgHour, // 주식 체결시간

            @JsonProperty("stck_prpr")
            String stckPrpr,          // 주식 현재가(분봉 종가)

            @JsonProperty("stck_oprc")
            String stckOprc,          // 주식 시가(첫 번째 체결가)

            @JsonProperty("stck_hgpr")
            String stckHgpr,          // 주식 최고가

            @JsonProperty("stck_lwpr")
            String stckLwpr,          // 주식 최저가

            @JsonProperty("cntg_vol")
            String cntgVol,            // 체결 거래량

            @JsonProperty("acml_tr_pbmn")
            String acmlTrPbmn      // 누적 거래대금
    ) {
    }
}