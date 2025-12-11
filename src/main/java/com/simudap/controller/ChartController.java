package com.simudap.controller;


import com.simudap.dto.kis.KisChartDataResponse;
import com.simudap.facade.ChartFacade;
import com.simudap.util.response.ApiResponse;
import com.simudap.util.response.Responses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Chart", description = "Chart API")
@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class ChartController {

    private final ChartFacade chartFacade;

    @GetMapping("/chart/kr/{stockCode}/{interval}")
    public ResponseEntity<ApiResponse<KisChartDataResponse>> getChart(@PathVariable String stockCode,
                                                                      @PathVariable String interval,
                                                                      @RequestParam(value = "from") String from,
                                                                      @RequestParam(value = "count") String count) {
        KisChartDataResponse response = chartFacade.getChart(stockCode, interval, from, count);
        return Responses.ok(response);
    }
}
