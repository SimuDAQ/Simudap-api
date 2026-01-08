package com.simudap.dto.websocket;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public record StockExecutionData(
        @Schema(description = "유가증권 단축 종목코드") String stockCode,
        @Schema(description = "주식 체결 시간") String executionTime,
        @Schema(description = "주식 현재가") String currentPrice,
        @Schema(description = "전일 대비 부호") String priceChangeSign,
        @Schema(description = "전일 대비") String priceChange,
        @Schema(description = "전일 대비율") String priceChangeRate,
        @Schema(description = "가중 평균 주식 가격") String weightedAveragePrice,
        @Schema(description = "주식 시가") String openPrice,
        @Schema(description = "주식 최고가") String highPrice,
        @Schema(description = "주식 최저가") String lowPrice,
        @Schema(description = "매도호가1") String askPrice1,
        @Schema(description = "매수호가1") String bidPrice1,
        @Schema(description = "체결 거래량") String executionVolume,
        @Schema(description = "누적 거래량") String accumulatedVolume,
        @Schema(description = "누적 거래 대금") String accumulatedTradeAmount,
        @Schema(description = "매도 체결 건수") String sellExecutionCount,
        @Schema(description = "매수 체결 건수") String buyExecutionCount,
        @Schema(description = "순매수 체결 건수") String netBuyExecutionCount,
        @Schema(description = "체결강도") String executionStrength,
        @Schema(description = "총 매도 수량") String totalSellVolume,
        @Schema(description = "총 매수 수량") String totalBuyVolume,
        @Schema(description = "체결구분") String executionType,
        @Schema(description = "매수비율") String buyRate,
        @Schema(description = "전일 거래량 대비 등락율") String volumeChangeRate,
        @Schema(description = "시가 시간") String openPriceTime,
        @Schema(description = "시가대비구분") String openPriceChangeSign,
        @Schema(description = "시가대비") String openPriceChange,
        @Schema(description = "최고가 시간") String highPriceTime,
        @Schema(description = "고가대비구분") String highPriceChangeSign,
        @Schema(description = "고가대비") String highPriceChange,
        @Schema(description = "최저가 시간") String lowPriceTime,
        @Schema(description = "저가대비구분") String lowPriceChangeSign,
        @Schema(description = "저가대비") String lowPriceChange,
        @Schema(description = "영업 일자") String businessDate,
        @Schema(description = "신 장운영 구분 코드") String marketOperationCode,
        @Schema(description = "거래정지 여부") String tradingHaltYn,
        @Schema(description = "매도호가 잔량1") String askVolume1,
        @Schema(description = "매수호가 잔량1") String bidVolume1,
        @Schema(description = "총 매도호가 잔량") String totalAskVolume,
        @Schema(description = "총 매수호가 잔량") String totalBidVolume,
        @Schema(description = "거래량 회전율") String volumeTurnoverRate,
        @Schema(description = "전일 동시간 누적 거래량") String previousDaySameTimeVolume,
        @Schema(description = "전일 동시간 누적 거래량 비율") String previousDaySameTimeVolumeRate,
        @Schema(description = "시간 구분 코드") String timeClassCode,
        @Schema(description = "임의종료구분코드") String marketClosureTypeCode,
        @Schema(description = "정적VI발동기준가") String viStandardPrice
) {
    public static StockExecutionData of(String stockCode, String[] recvvalue) {
        if (recvvalue.length < 46) {
            log.warn("StockExecutionData requires at least 46 fields, but got {}", recvvalue.length);
        }

        return new StockExecutionData(
                stockCode,                  // 유가증권 단축 종목코드
                recvvalue[1],              // 주식 체결 시간
                recvvalue[2],              // 주식 현재가
                recvvalue[3],              // 전일 대비 부호
                recvvalue[4],              // 전일 대비
                recvvalue[5],              // 전일 대비율
                recvvalue[6],              // 가중 평균 주식 가격
                recvvalue[7],              // 주식 시가
                recvvalue[8],              // 주식 최고가
                recvvalue[9],              // 주식 최저가
                recvvalue[10],             // 매도호가1
                recvvalue[11],             // 매수호가1
                recvvalue[12],             // 체결 거래량
                recvvalue[13],             // 누적 거래량
                recvvalue[14],             // 누적 거래 대금
                recvvalue[15],             // 매도 체결 건수
                recvvalue[16],             // 매수 체결 건수
                recvvalue[17],             // 순매수 체결 건수
                recvvalue[18],             // 체결강도
                recvvalue[19],             // 총 매도 수량
                recvvalue[20],             // 총 매수 수량
                recvvalue[21],             // 체결구분
                recvvalue[22],             // 매수비율
                recvvalue[23],             // 전일 거래량 대비 등락율
                recvvalue[24],             // 시가 시간
                recvvalue[25],             // 시가대비구분
                recvvalue[26],             // 시가대비
                recvvalue[27],             // 최고가 시간
                recvvalue[28],             // 고가대비구분
                recvvalue[29],             // 고가대비
                recvvalue[30],             // 최저가 시간
                recvvalue[31],             // 저가대비구분
                recvvalue[32],             // 저가대비
                recvvalue[33],             // 영업 일자
                recvvalue[34],             // 신 장운영 구분 코드
                recvvalue[35],             // 거래정지 여부
                recvvalue[36],             // 매도호가 잔량1
                recvvalue[37],             // 매수호가 잔량1
                recvvalue[38],             // 총 매도호가 잔량
                recvvalue[39],             // 총 매수호가 잔량
                recvvalue[40],             // 거래량 회전율
                recvvalue[41],             // 전일 동시간 누적 거래량
                recvvalue[42],             // 전일 동시간 누적 거래량 비율
                recvvalue[43],             // 시간 구분 코드
                recvvalue[44],             // 임의종료구분코드
                recvvalue[45]              // 정적VI발동기준가
        );
    }
}
