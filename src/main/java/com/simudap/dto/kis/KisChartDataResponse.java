package com.simudap.dto.kis;

import com.simudap.util.TimeUtils;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

public record KisChartDataResponse(
        String code,
        LocalDateTime nextDateTime,
        List<Candle> candles
) {
    public static KisChartDataResponse of(String stockCode, int interval, KisChartMinToday minToday) {
        KisChartMinToday.Output1 output1 = minToday.output1();
        String base = output1.stckPrdyClpr();
        List<Candle> candles = minToday.output2()
                .stream()
                .map(output2 -> {
                    return Candle.of(Long.parseLong(base), output2);
                })
                .sorted(Comparator.comparing(Candle::dateTime).reversed())
                .toList();

        Candle last = candles.getLast();
        LocalDateTime nextDateTime = last.dateTime().minusMinutes(interval);

        return new KisChartDataResponse(stockCode, nextDateTime, candles);
    }

    private record Candle(
            LocalDateTime dateTime,
            long base,
            long open,
            long high,
            long low,
            long close,
            long volume,
            long accumulatedAmount
    ) {
        public static Candle of(long base, KisChartMinToday.Output2 output2) {
            LocalDateTime dateTime = TimeUtils.toLocalDateTime2(output2.stckBsopDate() + output2.stckCntgHour());
            return new Candle(
                    dateTime,
                    base,
                    Long.parseLong(output2.stckOprc()),
                    Long.parseLong(output2.stckHgpr()),
                    Long.parseLong(output2.stckLwpr()),
                    Long.parseLong(output2.stckPrpr()),
                    Long.parseLong(output2.cntgVol()),
                    Long.parseLong(output2.acmlTrPbmn())
            );
        }
    }
}
