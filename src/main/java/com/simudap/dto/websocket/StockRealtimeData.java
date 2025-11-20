package com.simudap.dto.websocket;

import java.util.List;

/**
 * 실시간 주식 호가 데이터 DTO
 * KIS WebSocket으로부터 수신한 데이터를 클라이언트에게 전달
 */
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

    /**
     * 호가 단계별 가격 및 수량 정보
     */
    public record PriceLevel(
            String price,
            String volume
    ) {
    }

    /**
     * 예상 체결 정보
     */
    public record ExpectedTrade(
            String price,
            String volume,
            String totalVolume,
            String priceChange,
            String priceSign,
            String priceChangeRate
    ) {
    }
}
