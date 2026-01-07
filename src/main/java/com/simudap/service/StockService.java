package com.simudap.service;

import com.simudap.dto.stock.StockInfo;
import com.simudap.model.kospi.KospiFinancialInfo;
import com.simudap.model.kospi.KospiMaster;
import com.simudap.model.kospi.KospiTradingInfo;
import com.simudap.model.kospi.KospiTypeInfo;
import com.simudap.repository.kospi.KospiFinancialInfoRepository;
import com.simudap.repository.kospi.KospiMasterRepository;
import com.simudap.repository.kospi.KospiTradingInfoRepository;
import com.simudap.repository.kospi.KospiTypeInfoRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockService {

    private final KospiMasterRepository kospiMasterRepository;
    private final KospiTradingInfoRepository kospiTradingInfoRepository;
    private final KospiTypeInfoRepository kospiTypeInfoRepository;
    private final KospiFinancialInfoRepository kospiFinancialInfoRepository;

    // 정렬: 그룹코드가 ST(보통주, 우선주)인 종목 먼저 + 전일 거래량 순으로 10개
    public List<StockInfo> search(String name) {
        if (StringUtils.isBlank(name)) {
            // 검색어 없을 경우 전일 거래량 상위 10개 반환
            return getTop10ByPreviousDayVolume();
        }

        Map<Long, KospiMaster> baseInfoMap = kospiMasterRepository.findAllByNameKrContainingAndIsDeListed(name, false)
                .stream()
                .collect(Collectors.toMap(KospiMaster::getId, Function.identity()));

        List<KospiTradingInfo> tradingInfos = kospiTradingInfoRepository.findAllByKospiMasterIdIn(baseInfoMap.keySet())
                .stream()
                .toList();

        return converToStockInfo(tradingInfos, baseInfoMap);
    }

    private List<StockInfo> getTop10ByPreviousDayVolume() {
        List<KospiTradingInfo> top10 = kospiTradingInfoRepository.findTop10ByOrderByPreviousDayVolumeDesc();
        Set<Long> ids = top10
                .stream()
                .map(KospiTradingInfo::getKospiMasterId)
                .collect(Collectors.toSet());

        Map<Long, KospiMaster> baseInfoMap = kospiMasterRepository.findAllByIdInAndIsDeListed(ids, false)
                .stream()
                .collect(Collectors.toMap(KospiMaster::getId, kospi -> kospi));

        return converToStockInfo(top10, baseInfoMap);
    }

    private List<StockInfo> converToStockInfo(List<KospiTradingInfo> tradingInfos, Map<Long, KospiMaster> baseInfoMap) {
        Map<Long, KospiTypeInfo> typeInfoMap = kospiTypeInfoRepository.findAllByKospiMasterIdIn(baseInfoMap.keySet())
                .stream()
                .collect(Collectors.toMap(KospiTypeInfo::getKospiMasterId, Function.identity()));

        Map<Long, KospiFinancialInfo> financialInfoMap = kospiFinancialInfoRepository.findAllByKospiMasterIdIn(baseInfoMap.keySet())
                .stream()
                .collect(Collectors.toMap(KospiFinancialInfo::getKospiMasterId, Function.identity()));

        return tradingInfos
                .stream()
                .map(tradingInfo -> {
                    long fk = tradingInfo.getKospiMasterId();
                    KospiMaster kospiMaster = baseInfoMap.get(fk);
                    KospiTypeInfo kospiTypeInfo = typeInfoMap.get(fk);
                    KospiFinancialInfo kospiFinancialInfo = financialInfoMap.get(fk);
                    if (kospiMaster == null || kospiTypeInfo == null || kospiFinancialInfo == null) {
                        return null;
                    }

                    return StockInfo.of(kospiMaster, tradingInfo, kospiTypeInfo, kospiFinancialInfo);
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing((StockInfo info) -> !info.meta().groupCode().equals("ST"))
                        .thenComparing(Comparator.comparing(StockInfo::previousDayVolume).reversed()))
                .limit(10)
                .toList();
    }
}
