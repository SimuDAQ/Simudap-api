package com.simudap.dto.kis;

import com.simudap.enums.ChartInterval;
import com.simudap.enums.kis.KisRequestParam;
import com.simudap.error.BadRequestException;
import com.simudap.error.ResourceNotFoundException;
import com.simudap.util.TimeUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class KisChartDataRequest {
    // 현재 지원하는 분봉 : 1, 3, 5, 10, 15, 30, 60
    private static final List<Integer> MINUTE_INTERVALS = List.of(1, 3, 5, 10, 15, 30, 60);
    private static final int PERIOD_MAX_VALUE = 100;

    // tr_id
    private static final String CHART_MIN_TODAY_TR_ID = "FHKST03010200";
    private static final String CHART_MIN_PAST_TR_ID = "FHKST03010230";
    private static final String CHART_TIMEFRAME_TR_ID = "FHKST03010100";

    private final String stockCode;
    private final ChartInterval interval;
    private final int intervalValue;
    private final LocalDateTime from;
    private final String count;
    private final String trId;
    private static final int DEFAULT_INTERVAL_VALUE = 1;
    private final MultiValueMap<String, String> params;

    public static KisChartDataRequest parse(String stockCode, String interval, String from, String count) {
        return new KisChartDataRequest(stockCode, interval, from, count);
    }

    private KisChartDataRequest(String stockCode, String intervalStr, String from, String count) {
        String[] interval = intervalStr.split(":");
        LocalDateTime dateTime = TimeUtils.toLocalDateTime1(from);
        boolean isToday = isToday(dateTime.toLocalDate());

        if (interval.length < 2) {
            throw new BadRequestException("Invalid interval");
        }

        ChartInterval chartInterval = ChartInterval.from(interval[0], isToday);
        int intervalNum = Integer.parseInt(interval[1]);
        int intervalValue;

        if (chartInterval == ChartInterval.MIN_TODAY || chartInterval == ChartInterval.MIN_PAST) {
            intervalValue = MINUTE_INTERVALS
                    .stream()
                    .filter(value -> value == intervalNum)
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Invalid interval"));
        } else {
            // 분봉 이외에는 간격 1 고정
            intervalValue = DEFAULT_INTERVAL_VALUE;
        }

        this.stockCode = stockCode;
        this.interval = chartInterval;
        this.intervalValue = intervalValue;
        this.from = dateTime;
        this.count = count;

        // 분봉 조회일 경우 날짜 기준으로 당일 분봉 조회 or 과거 분봉 조회 결정
        if (chartInterval == ChartInterval.MIN_TODAY) {
            this.trId = CHART_MIN_TODAY_TR_ID;
            this.params = buildTodayParams(stockCode, dateTime);
        } else if (chartInterval == ChartInterval.MIN_PAST) {
            this.trId = CHART_MIN_PAST_TR_ID;
            this.params = buildPastParams(stockCode, dateTime);
        } else {
            this.trId = CHART_TIMEFRAME_TR_ID;
            this.params = buildPeriodParams(stockCode, chartInterval, dateTime);
        }
    }

    private boolean isToday(LocalDate searchDate) {
        LocalDate today = TimeUtils.seoulNow().toLocalDate();
        return searchDate.isEqual(today);
    }

    private MultiValueMap<String, String> buildTodayParams(String stockCode, LocalDateTime dateTime) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add(KisRequestParam.FID_COND_MRKT_DIV_CODE.name(), "J");
        map.add(KisRequestParam.FID_INPUT_ISCD.name(), stockCode);
        map.add(KisRequestParam.FID_INPUT_HOUR_1.name(), TimeUtils.toTimeString(dateTime));
        map.add(KisRequestParam.FID_PW_DATA_INCU_YN.name(), "Y");
        map.add(KisRequestParam.FID_ETC_CLS_CODE.name(), "");
        return map;
    }

    private MultiValueMap<String, String> buildPastParams(String stockCode, LocalDateTime dateTime) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add(KisRequestParam.FID_COND_MRKT_DIV_CODE.name(), "J");
        map.add(KisRequestParam.FID_INPUT_ISCD.name(), stockCode);
        map.add(KisRequestParam.FID_INPUT_HOUR_1.name(), TimeUtils.toTimeString(dateTime));
        map.add(KisRequestParam.FID_INPUT_DATE_1.name(), TimeUtils.toDateString(dateTime));
        map.add(KisRequestParam.FID_PW_DATA_INCU_YN.name(), "Y");
        map.add(KisRequestParam.FID_FAKE_TICK_INCU_YN.name(), "");
        return map;
    }

    private MultiValueMap<String, String> buildPeriodParams(String stockCode, ChartInterval interval, LocalDateTime dateTime) {
        LocalDateTime pastDateTime = getPastDateTime(interval, dateTime);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add(KisRequestParam.FID_COND_MRKT_DIV_CODE.name(), "J");
        map.add(KisRequestParam.FID_INPUT_ISCD.name(), stockCode);
        map.add(KisRequestParam.FID_INPUT_DATE_1.name(), TimeUtils.toDateString(pastDateTime));
        map.add(KisRequestParam.FID_INPUT_DATE_2.name(), TimeUtils.toDateString(dateTime));
        map.add(KisRequestParam.FID_PERIOD_DIV_CODE.name(), interval.getValue());
        map.add(KisRequestParam.FID_ORG_ADJ_PRC.name(), "0");
        return map;
    }

    private LocalDateTime getPastDateTime(ChartInterval interval, LocalDateTime dateTime) {
        // 기간 최대 100개 조회 가능
        if (interval == ChartInterval.DAY) {
            return dateTime.minusDays(PERIOD_MAX_VALUE);
        }
        if (interval == ChartInterval.WEEK) {
            return dateTime.minusWeeks(PERIOD_MAX_VALUE);
        }
        if (interval == ChartInterval.MONTH) {
            return dateTime.minusMonths(PERIOD_MAX_VALUE);
        }
        if (interval == ChartInterval.YEAR) {
            return dateTime.minusYears(PERIOD_MAX_VALUE);
        }

        return dateTime.minusDays(PERIOD_MAX_VALUE);
    }
}
