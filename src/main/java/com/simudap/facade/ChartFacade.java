package com.simudap.facade;

import com.simudap.dto.kis.KisChartDataRequest;
import com.simudap.service.KisApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChartFacade {

    private final KisApiService kisApiService;

    public void getChart(String stockCode, String interval, String from, String count) {
        KisChartDataRequest request = KisChartDataRequest.parse(stockCode, interval, from, count);
        kisApiService.getChartData(request);
    }
}
