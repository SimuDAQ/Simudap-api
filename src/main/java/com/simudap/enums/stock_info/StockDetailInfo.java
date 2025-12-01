package com.simudap.enums.stock_info;

import java.util.Map;

public interface StockDetailInfo {
    String getValue();

    int getFieldSpace();

    Map<Integer, StockDetailInfo> convertToMap();
}
