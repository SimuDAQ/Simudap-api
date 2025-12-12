package com.simudap.facade;

import com.simudap.dto.kis.KisChartDataRequest;
import com.simudap.dto.kis.KisChartDataResponse;
import com.simudap.error.ResourceNotFoundException;
import com.simudap.model.KisToken;
import com.simudap.model.kospi.KospiMaster;
import com.simudap.service.KisApiService;
import com.simudap.service.TokenService;
import com.simudap.service.kospi.KospiMasterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChartFacade {

    private final TokenService tokenService;
    private final KisApiService kisApiService;
    private final KospiMasterService kospiMasterService;

    public KisChartDataResponse getChart(String stockCode, String interval, String from, String count) {
        String shortCode = kospiMasterService.findByShortCode(stockCode)
                .map(KospiMaster::getShortCode)
                .orElseThrow(() -> new ResourceNotFoundException("There is no stock with code " + stockCode));
        KisToken kisToken = tokenService.getKisToken();

        KisChartDataRequest request = KisChartDataRequest.parse(shortCode, interval, from, count);
        return kisApiService.getChartData(request, kisToken);
    }
}
