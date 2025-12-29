package com.simudap.service;

import com.simudap.dto.stock.StockInfo;
import com.simudap.model.kospi.KospiFinancialInfo;
import com.simudap.model.kospi.KospiMaster;
import com.simudap.repository.kospi.KospiFinancialInfoRepository;
import com.simudap.repository.kospi.KospiMasterRepository;
import com.simudap.repository.kospi.KospiTradingInfoRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockService {

    private final KospiMasterRepository kospiMasterRepository;
    private final KospiTradingInfoRepository kospiTradingInfoRepository;
    private final KospiFinancialInfoRepository kospiFinancialInfoRepository;

    public List<StockInfo> search(String name) {
        if (StringUtils.isBlank(name)) {
            // 검색어 없을 경우 전일 시가총액 상위 10개 반환
            return getTop10ByPreviousDayMarkeyCap();
        }

        Map<Long, KospiMaster> baseInfoMap = kospiMasterRepository.findAllByNameKrContainingAndDeListed(name, false)
                .stream()
                .collect(Collectors.toMap(KospiMaster::getId, Function.identity()));

        return converToStockInfo(baseInfoMap);
    }

    private List<StockInfo> getTop10ByPreviousDayMarkeyCap() {
        Set<Long> ids = kospiFinancialInfoRepository.findTop10ByOrderByPreviousDayMarketCapDesc()
                .stream()
                .map(KospiFinancialInfo::getKospiMasterId)
                .collect(Collectors.toSet());

        Map<Long, KospiMaster> baseInfoMap = kospiMasterRepository.findAllByIdAndDeListed(ids, false)
                .stream()
                .collect(Collectors.toMap(KospiMaster::getId, kospi -> kospi));

        return converToStockInfo(baseInfoMap);
    }

    private List<StockInfo> converToStockInfo(Map<Long, KospiMaster> baseInfoMap) {
        return kospiTradingInfoRepository.findAllById(baseInfoMap.keySet())
                .stream()
                .map(tradingInfo -> {
                    long fk = tradingInfo.getKospiMasterId();
                    KospiMaster kospiMaster = baseInfoMap.get(fk);
                    if (kospiMaster == null) {
                        return null;
                    }

                    return StockInfo.of(kospiMaster, tradingInfo);
                })
                .filter(Objects::nonNull)
                .toList();
    }
}
