package com.simudap.dto.websocket;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;

public record StockRealtimeData(
        @Schema(title = "종목코드") String stockCode,
        @Schema(title = "영업시간") String businessTime,
        @Schema(title = "사간 구분 코드") String timeCode,
        @Schema(title = "매도호가 10단계") List<PriceLevel> askPrices,
        @Schema(title = "매수호가 10단계") List<PriceLevel> bidPrices,
        @Schema(title = "총매도호가 잔량") String totalAskVolume,
        @Schema(title = "총매도호가 잔량 증감") String totalAskVolumeChange,
        @Schema(title = "총매수호가 잔량") String totalBidVolume,
        @Schema(title = "총매수호가 잔량 증감") String totalBidVolumeChange,
        @Schema(title = "시간외 총매도호가 잔량") String afterHoursTotalAskVolume,
        @Schema(title = "시간외 총매수호가 잔량") String afterHoursTotalBidVolume,
        @Schema(title = "시간외 총매도호가 증감") String afterHoursTotalAskVolumeChange,
        @Schema(title = "시간외 총매수호가 증감") String afterHoursTotalBidVolumeChange,
        @Schema(title = "예상 체결 정보") ExpectedTrade expectedTrade,
        @Schema(title = "누적거래량") String accumulatedVolume,
        @Schema(title = "주식매매 구분코드") String tradeTypeCode
) {

    public static StockRealtimeData of(String[] recvvalue) {
        List<PriceLevel> askPrices = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            askPrices.add(new PriceLevel(
                    recvvalue[12 - i + 1],  // price
                    recvvalue[32 - i + 1]   // volume
            ));
        }

        List<PriceLevel> bidPrices = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            bidPrices.add(new PriceLevel(
                    recvvalue[12 + i],  // price
                    recvvalue[32 + i]   // volume
            ));
        }

        ExpectedTrade expectedTrade = new ExpectedTrade(
                recvvalue[47],
                recvvalue[48],
                recvvalue[49],
                recvvalue[50],
                recvvalue[51],
                recvvalue[52]
        );

        return new StockRealtimeData(
                recvvalue[0],
                recvvalue[1],
                recvvalue[2],
                askPrices,
                bidPrices,
                recvvalue[43],
                recvvalue[54],
                recvvalue[44],
                recvvalue[55],
                recvvalue[45],
                recvvalue[46],
                recvvalue[56],
                recvvalue[57],
                expectedTrade,
                recvvalue[53],
                recvvalue[58]
        );
    }

    private record PriceLevel(
            String price,
            String volume
    ) {
    }

    private record ExpectedTrade(
            @Schema(title = "예상 체결가") String price,
            @Schema(title = "예상 체결량") String volume,
            @Schema(title = "예상 거래량") String totalVolume,
            @Schema(title = "예상체결 대비") String priceChange,
            @Schema(title = "부호") String priceSign,
            @Schema(title = "예상 체결 전일대비율") String priceChangeRate
    ) {
    }
}
