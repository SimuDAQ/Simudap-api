package com.simudap.dto.websocket;

import io.swagger.v3.oas.annotations.media.Schema;

public record StockExecutionData(
        @Schema(title = "종목코드") String stockCode,
        @Schema(title = "영업시간") String businessTime,
        @Schema(title = "체결시간") String executionTime,
        @Schema(title = "현재가") String currentPrice,
        @Schema(title = "전일대비부호") String priceChangeSign,
        @Schema(title = "전일대비") String priceChange,
        @Schema(title = "전일대비율") String priceChangeRate,
        @Schema(title = "가중평균가") String weightedAveragePrice,
        @Schema(title = "시가") String openPrice,
        @Schema(title = "고가") String highPrice,
        @Schema(title = "저가") String lowPrice,
        @Schema(title = "매수호가") String askPrice,
        @Schema(title = "매도호가") String bidPrice,
        @Schema(title = "누적거래량") String accumulatedVolume,
        @Schema(title = "누적거래대금") String accumulatedAmount,
        @Schema(title = "시가시간") String openTime,
        @Schema(title = "고가시간") String highTime,
        @Schema(title = "저가시간") String lowTime,
        @Schema(title = "체결강도") String executionStrength,
        @Schema(title = "시장구분") String marketType,
        @Schema(title = "체결구분") String executionType,
        @Schema(title = "매도체결건수") String askExecutionCount,
        @Schema(title = "매수체결건수") String bidExecutionCount,
        @Schema(title = "체결량") String executionVolume
) {
    public static StockExecutionData of(String[] recvvalue) {
        return new StockExecutionData(
                recvvalue[0],   // 종목코드
                recvvalue[1],   // 영업시간
                recvvalue[2],   // 체결시간
                recvvalue[3],   // 현재가
                recvvalue[4],   // 전일대비부호
                recvvalue[5],   // 전일대비
                recvvalue[6],   // 전일대비율
                recvvalue[7],   // 가중평균가
                recvvalue[8],   // 시가
                recvvalue[9],   // 고가
                recvvalue[10],  // 저가
                recvvalue[11],  // 매수호가
                recvvalue[12],  // 매도호가
                recvvalue[13],  // 누적거래량
                recvvalue[14],  // 누적거래대금
                recvvalue[15],  // 시가시간
                recvvalue[16],  // 고가시간
                recvvalue[17],  // 저가시간
                recvvalue[18],  // 체결강도
                recvvalue[19],  // 시장구분
                recvvalue[20],  // 체결구분
                recvvalue[21],  // 매도체결건수
                recvvalue[22],  // 매수체결건수
                recvvalue[23]   // 체결량
        );
    }
}
