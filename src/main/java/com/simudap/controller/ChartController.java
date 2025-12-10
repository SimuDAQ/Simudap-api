package com.simudap.controller;


import com.simudap.facade.ChartFacade;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Chart", description = "Chart API")
@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class ChartController {

    private final ChartFacade chartFacade;

    @GetMapping("/chart/kr/{stockCode}/{interval}")
    public void getChart(@PathVariable String stockCode,
                         @PathVariable String interval,
                         @RequestParam(value = "from") String from,
                         @RequestParam(value = "count") String count) {

        chartFacade.getChart(stockCode, interval, from, count);
    }
}
