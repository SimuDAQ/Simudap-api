package com.simudap.controller;

import com.simudap.facade.StockInformationFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class HelloController {

    private final StockInformationFacade stockInformationFacade;

    @GetMapping("/hello")
    public String hello() {
        return "Hello";
    }

    @GetMapping("/download")
    public void testDownload() {
        stockInformationFacade.updateStockInformation();
    }
}
