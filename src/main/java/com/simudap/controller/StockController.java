package com.simudap.controller;


import com.simudap.dto.stock.StockInfo;
import com.simudap.facade.StockFacade;
import com.simudap.util.response.ApiResponse;
import com.simudap.util.response.ListResponse;
import com.simudap.util.response.Responses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Stock", description = "Stock API")
@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class StockController {
    private final StockFacade stockFacade;

    @GetMapping("/stocks")
    public ResponseEntity<ApiResponse<ListResponse<StockInfo, Void>>> search(@RequestParam(value = "name", required = false) String name) {
        List<StockInfo> response = stockFacade.search(name);
        return Responses.ok(response);
    }
}
