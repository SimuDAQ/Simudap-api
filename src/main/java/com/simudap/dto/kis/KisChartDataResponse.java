package com.simudap.dto.kis;

import com.simudap.enums.ChartInterval;
import com.simudap.util.TimeUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public class KisChartDataResponse {
    private final String stockCode;
    private final LocalDateTime nextDateTime;
    private final List<Chart> candles;

    private KisChartDataResponse(KisChartDataRequest request, List<KisChartMin> mins) {
        if (mins.isEmpty()) {
            throw new IllegalArgumentException("KisChartMin list is empty");
        }

        ChartInterval interval = request.getInterval();
        int intervalValue = request.getIntervalValue();
        String stockCode = request.getStockCode();
        int requestCount = request.getCount();

        List<Chart> charts = mins.stream()
                .map(this::convertToCharts)
                .flatMap(Collection::stream)
                .sorted(Comparator.comparing(Chart::dateTime).reversed())
                .distinct()
                .toList();

        List<Chart> processedCharts = fillMissingAfternoonData(charts);
        List<Chart> merged = intervalValue > 1 ? mergeCandles(processedCharts, intervalValue) : processedCharts;

        List<Chart> sortedCharts = merged.stream()
                .sorted(Comparator.comparing(Chart::dateTime).reversed())
                .limit(requestCount)
                .toList();

        LocalDateTime nextDateTime = getNextDateTime(interval, intervalValue, sortedCharts.getLast().dateTime());

        this.stockCode = stockCode;
        this.nextDateTime = nextDateTime;
        this.candles = sortedCharts;
    }

    public static KisChartDataResponse of(KisChartDataRequest request, List<KisChartMin> mins) {
        return new KisChartDataResponse(request, mins);
    }

    public static KisChartDataResponse of(String stockCode, ChartInterval interval, int intervalValue, KisChartPeriod period) {
        return new KisChartDataResponse(stockCode, interval, intervalValue, period);
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

    private List<Chart> convertToCharts(KisChartMin min) {
        KisChartMin.CurrentStockInfo stockInfo = min.currentStockInfo();
        return min.chartDataList()
                .stream()
                .map(data -> Chart.of(Long.parseLong(stockInfo.previousDayClosingPrice()), data))
                .toList();
    }

    private List<Chart> mergeCandles(List<Chart> charts, int intervalValue) {
        // 1분봉 데이터를 시간 기준으로 intervalValue 단위로 병합
        // 예: 3분봉의 경우 09:01~09:03 → 09:03 3분봉, 09:04~09:06 → 09:06 3분봉
        Map<Long, List<Chart>> groupedByTime = charts.stream()
                .collect(Collectors.groupingBy(chart -> {
                    LocalDateTime dt = chart.dateTime();
                    long totalMinutes = TimeUtils.toMinutesFromEpoch(dt);
                    return (totalMinutes + intervalValue - 1) / intervalValue;
                }));

        return groupedByTime.values().stream()
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

    private List<Chart> fillMissingAfternoonData(List<Chart> charts) {
        // 3시 19분 ~ 29 분 동시 호가 데이터 생성
        List<Chart> additionalData = charts.stream()
                .filter(chart -> chart.dateTime().getHour() == 15 && chart.dateTime().getMinute() == 19)
                .map(this::creatMissingData)
                .flatMap(Collection::stream)
                .toList();

        List<Chart> result = new ArrayList<>(charts);
        result.addAll(additionalData);
        return result;
    }

    private List<Chart> creatMissingData(Chart chart319) {
        // 3시 20 ~ 29 데이터는 19분 종가 데이터로 생성
        List<Chart> additionalCharts = new ArrayList<>();
        long closePrice = chart319.close();
        long base = chart319.base();
        LocalDateTime baseDateTime = chart319.dateTime();

        for (int i = 1; i <= 10; i++) {
            LocalDateTime newDateTime = baseDateTime.plusMinutes(i);
            Chart newChart = new Chart(
                    newDateTime,
                    base,
                    closePrice,
                    closePrice,
                    closePrice,
                    closePrice,
                    0L,
                    chart319.accumulatedAmount()
            );
            additionalCharts.add(newChart);
        }

        return additionalCharts;
    }

    private LocalDateTime getNextDateTime(ChartInterval interval, int intervalValue, LocalDateTime lastCandleTime) {
        return switch (interval) {
            case DAY -> lastCandleTime.minusDays(intervalValue);
            case WEEK -> lastCandleTime.minusWeeks(intervalValue);
            case MONTH -> lastCandleTime.minusMonths(intervalValue);
            case YEAR -> lastCandleTime.minusYears(intervalValue);
            case MIN -> lastCandleTime.minusMinutes(intervalValue);
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
