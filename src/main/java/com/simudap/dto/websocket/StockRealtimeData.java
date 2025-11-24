package com.simudap.dto.websocket;

import java.util.ArrayList;
import java.util.List;

public record StockRealtimeData(
        String stockCode,
        String businessTime,
        String timeCode,
        List<PriceLevel> askPrices,  // 매도호가 (10단계)
        List<PriceLevel> bidPrices,  // 매수호가 (10단계)
        String totalAskVolume,
        String totalAskVolumeChange,
        String totalBidVolume,
        String totalBidVolumeChange,
        String afterHoursTotalAskVolume,
        String afterHoursTotalBidVolume,
        String afterHoursTotalAskVolumeChange,
        String afterHoursTotalBidVolumeChange,
        ExpectedTrade expectedTrade,
        String accumulatedVolume,
        String tradeTypeCode
) {

    public static StockRealtimeData of(String[] recvvalue) {
        // 매도호가 (10단계)
        List<PriceLevel> askPrices = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            askPrices.add(new PriceLevel(
                    recvvalue[12 - i + 1],  // price
                    recvvalue[32 - i + 1]   // volume
            ));
        }

        // 매수호가 (10단계)
        List<PriceLevel> bidPrices = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            bidPrices.add(new PriceLevel(
                    recvvalue[12 + i],  // price
                    recvvalue[32 + i]   // volume
            ));
        }

        // 예상 체결 정보
        ExpectedTrade expectedTrade = new ExpectedTrade(
                recvvalue[47],  // price
                recvvalue[48],  // volume
                recvvalue[49],  // totalVolume
                recvvalue[50],  // priceChange
                recvvalue[51],  // priceSign
                recvvalue[52]   // priceChangeRate
        );

        return new StockRealtimeData(
                recvvalue[0],   // stockCode
                recvvalue[1],   // businessTime
                recvvalue[2],   // timeCode
                askPrices,
                bidPrices,
                recvvalue[43],  // totalAskVolume
                recvvalue[54],  // totalAskVolumeChange
                recvvalue[44],  // totalBidVolume
                recvvalue[55],  // totalBidVolumeChange
                recvvalue[45],  // afterHoursTotalAskVolume
                recvvalue[46],  // afterHoursTotalBidVolume
                recvvalue[56],  // afterHoursTotalAskVolumeChange
                recvvalue[57],  // afterHoursTotalBidVolumeChange
                expectedTrade,
                recvvalue[53],  // accumulatedVolume
                recvvalue[58]   // tradeTypeCode
        );
    }

    /**
     * 호가 단계별 가격 및 수량 정보
     */
    private record PriceLevel(
            String price,
            String volume
    ) {
    }

    /**
     * 예상 체결 정보
     */
    private record ExpectedTrade(
            String price,
            String volume,
            String totalVolume,
            String priceChange,
            String priceSign,
            String priceChangeRate
    ) {
    }
}
