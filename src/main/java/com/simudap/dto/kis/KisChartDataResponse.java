package com.simudap.dto.kis;

import com.simudap.enums.ChartInterval;
import com.simudap.util.TimeUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class KisChartDataResponse {
    private final String stockCode;
    private final LocalDateTime nextDateTime;
    private final List<Chart> candles;

    private KisChartDataResponse(String stockCode, ChartInterval interval, int intervalValue, KisChartMin minToday) {
        KisChartMin.CurrentStockInfo stockInfo = minToday.currentStockInfo();
        List<Chart> charts = minToday.chartDataList()
                .stream()
                .map(data -> Chart.of(Long.parseLong(stockInfo.previousDayClosingPrice()), data))
                .sorted(Comparator.comparing(Chart::dateTime).reversed())
                .toList();

        LocalDateTime nextDateTime = getNextDateTime(interval, intervalValue, charts.getLast().dateTime());

        this.stockCode = stockCode;
        this.nextDateTime = nextDateTime;
        this.candles = charts;
    }

    private KisChartDataResponse(String stockCode, ChartInterval interval, int intervalValue, KisChartPeriod period) {
        KisChartPeriod.CurrentStockInfo stockInfo = period.currentStockInfo();
        List<Chart> charts = period.chartDataList()
                .stream()
                .map(data -> Chart.of(Long.parseLong(stockInfo.previousDayClosingPrice()), data))
                .sorted(Comparator.comparing(Chart::dateTime).reversed())
                .toList();

        LocalDateTime nextDateTime = getNextDateTime(interval, intervalValue, charts.getLast().dateTime());

        this.stockCode = stockCode;
        this.nextDateTime = nextDateTime;
        this.candles = charts;
    }

    public static KisChartDataResponse of(String stockCode, ChartInterval interval, int intervalValue, KisChartMin minToday) {
        return new KisChartDataResponse(stockCode, interval, intervalValue, minToday);
    }

    public static KisChartDataResponse of(String stockCode, ChartInterval interval, int intervalValue, KisChartPeriod period) {
        return new KisChartDataResponse(stockCode, interval, intervalValue, period);
    }

    private LocalDateTime getNextDateTime(ChartInterval interval, int intervalValue, LocalDateTime lastCandleTime) {
        return switch (interval) {
            case DAY -> lastCandleTime.minusDays(intervalValue);
            case WEEK -> lastCandleTime.minusWeeks(intervalValue);
            case MONTH -> lastCandleTime.minusMonths(intervalValue);
            case YEAR -> lastCandleTime.minusYears(intervalValue);
            case MIN_TODAY, MIN_PAST -> lastCandleTime.minusMinutes(intervalValue);
        };
    }

    private record Chart(
            LocalDateTime dateTime,
            long base,
            long open,
            long high,
            long low,
            long close,
            long volume,
            long accumulatedAmount
    ) {
        public static Chart of(long base, KisChartMin.ChartData chartData) {
            LocalDateTime dateTime = TimeUtils.toLocalDateTime2(chartData.businessDate() + chartData.tradingTime());
            return new Chart(
                    dateTime,
                    base,
                    Long.parseLong(chartData.openingPrice()),
                    Long.parseLong(chartData.highPrice()),
                    Long.parseLong(chartData.lowPrice()),
                    Long.parseLong(chartData.closingPrice()),
                    Long.parseLong(chartData.tradingVolume()),
                    Long.parseLong(chartData.accumulatedTradingAmount())
            );
        }

        public static Chart of(long base, KisChartPeriod.ChartData chartData) {
            LocalDateTime dateTime = TimeUtils.toLocalDateTime3(chartData.businessDate());
            return new Chart(
                    dateTime,
                    base,
                    Long.parseLong(chartData.openingPrice()),
                    Long.parseLong(chartData.highPrice()),
                    Long.parseLong(chartData.lowPrice()),
                    Long.parseLong(chartData.closingPrice()),
                    Long.parseLong(chartData.accumulatedVolume()),
                    Long.parseLong(chartData.accumulatedTradingAmount())
            );
        }
    }
}
