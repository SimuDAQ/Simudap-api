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

import java.time.LocalDateTime;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class KisChartDataRequest {
    // 현재 지원하는 분봉 : 1, 3, 5, 10, 15, 30, 60
    private static final List<Integer> MINUTE_INTERVALS = List.of(1, 3, 5, 10, 15, 30, 60);
    private static final int PERIOD_MAX_VALUE = 100;
    private static final int DEFAULT_INTERVAL_VALUE = 1;

    // tr_id
    private static final String CHART_MIN_TR_ID = "FHKST03010230";
    private static final String CHART_PERIOD_TR_ID = "FHKST03010100";

    private final String stockCode;
    private final ChartInterval interval;
    private final int intervalValue;
    private final LocalDateTime from;
    private final int count;
    private final String trId;
    private final MultiValueMap<String, String> params;

    private KisChartDataRequest(String stockCode, String intervalStr, LocalDateTime from, String count) {
        String[] interval = intervalStr.split(":");
//        LocalDateTime dateTime = TimeUtils.toLocalDateTime1(from);

        if (interval.length < 2) {
            throw new BadRequestException("Invalid interval");
        }

        ChartInterval chartInterval = ChartInterval.from(interval[0]);
        int intervalNum = Integer.parseInt(interval[1]);
        int intervalValue;

        if (chartInterval == ChartInterval.MIN) {
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
        this.from = from;
        this.count = Integer.parseInt(count);

        if (chartInterval == ChartInterval.MIN) {
            this.trId = CHART_MIN_TR_ID;
            this.params = buildMinParams(from);
        } else {
            this.trId = CHART_PERIOD_TR_ID;
            this.params = buildPeriodParams(from);
        }
    }

    public static KisChartDataRequest parse(String stockCode, String interval, LocalDateTime from, String count) {
        return new KisChartDataRequest(stockCode, interval, from, count);
    }

    public MultiValueMap<String, String> buildMinParams(LocalDateTime dateTime) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add(KisRequestParam.FID_COND_MRKT_DIV_CODE.name(), "UN");
        map.add(KisRequestParam.FID_INPUT_ISCD.name(), this.stockCode);
        map.add(KisRequestParam.FID_INPUT_HOUR_1.name(), TimeUtils.toTimeString(dateTime));
        map.add(KisRequestParam.FID_INPUT_DATE_1.name(), TimeUtils.toDateString(dateTime));
        map.add(KisRequestParam.FID_PW_DATA_INCU_YN.name(), "Y");
        map.add(KisRequestParam.FID_FAKE_TICK_INCU_YN.name(), "");
        return map;
    }

    private MultiValueMap<String, String> buildPeriodParams(LocalDateTime dateTime) {
        LocalDateTime pastDateTime = getPastDateTime(dateTime);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add(KisRequestParam.FID_COND_MRKT_DIV_CODE.name(), "UN");
        map.add(KisRequestParam.FID_INPUT_ISCD.name(), this.stockCode);
        map.add(KisRequestParam.FID_INPUT_DATE_1.name(), TimeUtils.toDateString(pastDateTime));
        map.add(KisRequestParam.FID_INPUT_DATE_2.name(), TimeUtils.toDateString(dateTime));
        map.add(KisRequestParam.FID_PERIOD_DIV_CODE.name(), this.interval.getValue());
        map.add(KisRequestParam.FID_ORG_ADJ_PRC.name(), "0");
        return map;
    }

    private LocalDateTime getPastDateTime(LocalDateTime dateTime) {
        // 기간 최대 100개 조회 가능
        if (this.interval == ChartInterval.DAY) {
            return dateTime.minusDays(PERIOD_MAX_VALUE);
        }
        if (this.interval == ChartInterval.WEEK) {
            return dateTime.minusWeeks(PERIOD_MAX_VALUE);
        }
        if (this.interval == ChartInterval.MONTH) {
            return dateTime.minusMonths(PERIOD_MAX_VALUE);
        }
        if (this.interval == ChartInterval.YEAR) {
            return dateTime.minusYears(PERIOD_MAX_VALUE);
        }

        return dateTime.minusDays(PERIOD_MAX_VALUE);
    }
}
