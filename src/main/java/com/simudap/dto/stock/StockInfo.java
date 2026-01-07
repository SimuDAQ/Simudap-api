package com.simudap.dto.stock;

import com.simudap.model.kospi.KospiFinancialInfo;
import com.simudap.model.kospi.KospiMaster;
import com.simudap.model.kospi.KospiTradingInfo;
import com.simudap.model.kospi.KospiTypeInfo;

public record StockInfo(
        long id,
        String shortCode,
        String standardCode,
        String nameKr,
        int basePrice,
        int previousDayVolume,
        MetaData meta
) {
    public static StockInfo of(KospiMaster kospiMaster, KospiTradingInfo tradingInfo, KospiTypeInfo kospiTypeInfo, KospiFinancialInfo kospiFinancialInfo) {
        MetaData metaData = MetaData.of(kospiTypeInfo, kospiFinancialInfo);
        return new StockInfo(
                kospiMaster.getId(),
                kospiMaster.getShortCode(),
                kospiMaster.getStandardCode(),
                kospiMaster.getNameKr(),
                tradingInfo.getBasePrice(),
                tradingInfo.getPreviousDayVolume(),
                metaData
        );
    }

    public record MetaData(
            String groupCode,
            Integer previousDayMarketCap
    ) {
        public static MetaData of(KospiTypeInfo kospiTypeInfo, KospiFinancialInfo kospiFinancialInfo) {
            return new MetaData(
                    kospiTypeInfo.getSecurityGroupCode(),
                    kospiFinancialInfo.getPreviousDayMarketCap()
            );
        }
    }
}
