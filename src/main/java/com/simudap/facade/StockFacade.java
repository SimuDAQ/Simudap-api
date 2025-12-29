package com.simudap.facade;

import com.simudap.dto.stock.StockInfo;
import com.simudap.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class StockFacade {

    private final StockService stockService;

    public List<StockInfo> search(String name) {
        return stockService.search(name);
    }
}
