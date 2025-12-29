package com.simudap.dto.stock;

import com.simudap.model.kospi.KospiMaster;
import com.simudap.model.kospi.KospiTradingInfo;

public record StockInfo(
        long id,
        String shortCode,
        String standardCode,
        String nameKr,
        int basePrice
) {
    public static StockInfo of(KospiMaster kospiMaster, KospiTradingInfo tradingInfo) {
        return new StockInfo(
                kospiMaster.getId(),
                kospiMaster.getShortCode(),
                kospiMaster.getStandardCode(),
                kospiMaster.getNameKr(),
                tradingInfo.getBasePrice()
        );
    }
}
