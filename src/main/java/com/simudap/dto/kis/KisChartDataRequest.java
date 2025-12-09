package com.simudap.dto.kis;

import com.simudap.error.BadRequestException;
import com.simudap.error.ResourceNotFoundException;
import com.simudap.util.DateTimeUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public record KisChartDataRequest(
        String stockCode,
        ChartInterval interval,
        int intervalValue,
        LocalDateTime from,
        String count,
        String uriPath,
        MultiValueMap<String, String> params
) {

    private static final String CHART_MIN_TODAY_PATH = "/uapi/domestic-stock/v1/quotations/inquire-time-itemchartprice";
    private static final String CHART_MIN_PAST_PATH = "/uapi/domestic-stock/v1/quotations/inquire-time-dailychartprice";
    private static final String CHART_TIMEFRAME_PATH = "/uapi/domestic-stock/v1/quotations/inquire-daily-itemchartprice";

    private static final List<Integer> MINUTE_INTERVALS = List.of(1, 3, 5, 10, 15, 30, 60);
    private static final int DEFAULT_INTERVAL_VALUE = 1;

    public static KisChartDataRequest parse(String stockCode, String interval, String from, String count) {
        String[] split = interval.split(":");

        if (split.length < 2) {
            throw new BadRequestException("Invalid interval");
        }

        ChartInterval chartInterval = ChartInterval.from(split[0]);
        int intervalNum = Integer.parseInt(split[1]);
        int intervalValue;

        if (chartInterval == ChartInterval.MIN) {
            intervalValue = MINUTE_INTERVALS
                    .stream()
                    .filter(value -> value == intervalNum)
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Invalid interval"));
        } else {
            intervalValue = DEFAULT_INTERVAL_VALUE;
        }

        LocalDateTime dateTime = DateTimeUtils.toLocalDateTime(from);
        String uriPath = getPath(chartInterval, dateTime.toLocalDate());
        MultiValueMap<String, String> params = buildParams();

        return new KisChartDataRequest(stockCode, chartInterval, intervalValue, dateTime, count, uriPath, params);
    }

    private static String getPath(ChartInterval chartInterval, LocalDate date) {
        if (chartInterval == ChartInterval.MIN) {
            LocalDate today = LocalDate.now();
            if (date.isBefore(today)) {
                return CHART_MIN_PAST_PATH;
            } else {
                return CHART_MIN_TODAY_PATH;
            }
        }

        return CHART_TIMEFRAME_PATH;
    }

    private static MultiValueMap<String, String> buildParams() {
        return new LinkedMultiValueMap<>();
    }

    public enum ChartInterval {
        MIN,
        DAY,
        WEEK,
        MONTH,
        YEAR,
        ;

        public static ChartInterval from(String interval) {
            return Arrays.stream(ChartInterval.values())
                    .filter(value -> value.name().equalsIgnoreCase(interval))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Not found interval " + interval));
        }
    }
}
