package com.simudap.facade;

import com.simudap.dto.stock.StockInfoResponse;
import com.simudap.enums.stock_info.KospiDetailInfo;
import com.simudap.service.StockInformationDownloadService;
import com.simudap.service.kospi.KospiInfoUpdateService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class StockInformationFacade {

    private static final int KOSPI_PART2_LENGTH = 227;
    private static final String KOSPI_URL = "https://new.real.download.dws.co.kr/common/master/kospi_code.mst.zip";
    private final StockInformationDownloadService downloadService;
    private final KospiInfoUpdateService kospiInfoUpdateService;

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void updateStockInformation() {
        StockInfoResponse kospi = downloadService.downloadFile(KospiDetailInfo.TYPE, KOSPI_PART2_LENGTH, KOSPI_URL);
        kospiInfoUpdateService.update(kospi);
    }
}
