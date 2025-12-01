package com.simudap.service.kospi;

import com.simudap.dto.stock.StockInfoResponse;
import com.simudap.model.kospi.KospiMaster;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KospiInfoUpdateService {

    private final KospiMasterService kospiMasterService;
    private final KospiTypeInfoService kospiTypeInfoService;
    private final KospiTradingStatusFlagService kospiTradingStatusFlagService;
    private final KospiTradingInfoService kospiTradingInfoService;
    private final KospiSectorInfoService kospiSectorInfoService;
    private final KospiRiskFlagService kospiRiskFlagService;
    private final KospiListingBasicInfoService kospiListingBasicInfoService;
    private final KospiIndustryInfoService kospiIndustryInfoService;
    private final KospiIndexInfoService kospiIndexInfoService;
    private final KospiFinancialInfoService kospiFinancialInfoService;
    private final KospiCorporateActionFlagService kospiCorporateActionFlagService;

    @Transactional
    public void update(StockInfoResponse kospi) {
        List<KospiMaster> updatedBaseInfos = kospiMasterService.upsert(kospi.part1());
        upsertDetailInfo(updatedBaseInfos, kospi.part2());

        // 상장폐지 주식 종목 제거
        kospiMasterService.removeDeListed(kospi.part1());
    }

    private void upsertDetailInfo(List<KospiMaster> updatedBaseInfo, Map<String, Map<String, String>> part2Map) {
        Set<Long> upsertedIds = updatedBaseInfo
                .stream()
                .map(KospiMaster::getId)
                .collect(Collectors.toSet());

        kospiTypeInfoService.upsert(updatedBaseInfo, part2Map, upsertedIds);
        kospiTradingStatusFlagService.upsert(updatedBaseInfo, part2Map, upsertedIds);
        kospiTradingInfoService.upsert(updatedBaseInfo, part2Map, upsertedIds);
        kospiSectorInfoService.upsert(updatedBaseInfo, part2Map, upsertedIds);
        kospiRiskFlagService.upsert(updatedBaseInfo, part2Map, upsertedIds);
        kospiListingBasicInfoService.upsert(updatedBaseInfo, part2Map, upsertedIds);
        kospiIndustryInfoService.upsert(updatedBaseInfo, part2Map, upsertedIds);
        kospiIndexInfoService.upsert(updatedBaseInfo, part2Map, upsertedIds);
        kospiFinancialInfoService.upsert(updatedBaseInfo, part2Map, upsertedIds);
        kospiCorporateActionFlagService.upsert(updatedBaseInfo, part2Map, upsertedIds);
    }
}
