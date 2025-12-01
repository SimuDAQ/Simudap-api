package com.simudap.dto.stock;

import java.util.Map;

public record StockInfoResponse(
        Map<String, StockBaseInfo> part1,
        Map<String, Map<String, String>> part2
) {
}
