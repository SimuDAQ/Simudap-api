package com.simudap.controller;

import com.simudap.scheduler.StockInformationScheduler;
import com.simudap.util.response.ApiResponse;
import com.simudap.util.response.Responses;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Hello", description = "Hello API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class HelloController {

    private final StockInformationScheduler scheduler;

    @Operation(summary = "Hello!")
    @GetMapping("/hello")
    public ResponseEntity<ApiResponse<String>> hello() {
        return Responses.ok("Hello");
    }

    @Hidden
    @PutMapping("/download/test")
    public ResponseEntity<Void> downLoadTest() {
        scheduler.updateStockInformation();
        return Responses.ok();
    }
}
