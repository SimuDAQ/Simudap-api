package com.simudap.dto.kis;

import com.simudap.enums.ChartInterval;
import com.simudap.util.TimeUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public class KisChartDataResponse {
    private final String stockCode;
    private final LocalDateTime nextDateTime;
    private final List<Chart> candles;

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

    private KisChartDataResponse(String stockCode, ChartInterval interval, int intervalValue, List<KisChartMin> mins) {
        if (mins.isEmpty()) {
            throw new IllegalArgumentException("KisChartMin list is empty");
        }

        KisChartMin.CurrentStockInfo stockInfo = mins.getFirst().currentStockInfo();


        List<Chart> charts = mins.stream()
                .flatMap(min -> min.chartDataList().stream())
                .map(data -> Chart.of(Long.parseLong(stockInfo.previousDayClosingPrice()), data))
                .sorted(Comparator.comparing(Chart::dateTime))
                .distinct()
                .toList();

        List<Chart> processedCharts = intervalValue > 1 ? mergeCandles(charts, intervalValue) : charts;

        List<Chart> sortedCharts = processedCharts.stream()
                .sorted(Comparator.comparing(Chart::dateTime).reversed())
                .toList();

        LocalDateTime nextDateTime = getNextDateTime(interval, intervalValue, sortedCharts.getLast().dateTime());

        this.stockCode = stockCode;
        this.nextDateTime = nextDateTime;
        this.candles = sortedCharts;
    }

    public static KisChartDataResponse of(String stockCode, ChartInterval interval, int intervalValue, List<KisChartMin> mins) {
        return new KisChartDataResponse(stockCode, interval, intervalValue, mins);
    }

    public static KisChartDataResponse of(String stockCode, ChartInterval interval, int intervalValue, KisChartPeriod period) {
        return new KisChartDataResponse(stockCode, interval, intervalValue, period);
    }

    // 1분봉 데이터를 시간 기준으로 intervalValue 단위로 병합
    // 예: 3분봉의 경우 09:01~09:03 → 09:03 3분봉, 09:04~09:06 → 09:06 3분봉
    private List<Chart> mergeCandles(List<Chart> charts, int intervalValue) {
        Map<Long, List<Chart>> groupedByTime = charts.stream()
                .collect(Collectors.groupingBy(chart -> {
                    LocalDateTime dt = chart.dateTime();
                    long totalMinutes = TimeUtils.toMinutesFromEpoch(dt);
                    return (totalMinutes + intervalValue - 1) / intervalValue;
                }));

        return groupedByTime.values().stream()
                .filter(chartList -> chartList.size() == intervalValue) // 완전한 봉만 생성
                .map(group -> {
                    group.sort(Comparator.comparing(Chart::dateTime));

                    Chart first = group.getFirst();
                    Chart last = group.getLast();

                    long open = first.open();
                    long close = last.close();
                    long high = group.stream().mapToLong(Chart::high).max().orElse(0);
                    long low = group.stream().mapToLong(Chart::low).min().orElse(0);
                    long volume = group.stream().mapToLong(Chart::volume).sum();
                    long accumulatedAmount = last.accumulatedAmount();
                    LocalDateTime dateTime = last.dateTime();

                    return new Chart(dateTime, first.base(), open, high, low, close, volume, accumulatedAmount);
                })
                .sorted(Comparator.comparing(Chart::dateTime))
                .toList();
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
