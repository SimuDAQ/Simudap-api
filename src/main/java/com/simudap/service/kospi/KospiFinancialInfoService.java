package com.simudap.service.kospi;

import com.simudap.enums.stock_info.KospiDetailInfo;
import com.simudap.model.kospi.KospiFinancialInfo;
import com.simudap.model.kospi.KospiMaster;
import com.simudap.repository.kospi.KospiFinancialInfoRepository;
import com.simudap.util.TypeConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KospiFinancialInfoService {
    private final KospiFinancialInfoRepository kospiFinancialInfoRepository;

    public void upsert(List<KospiMaster> updatedPart1s, Map<String, Map<String, String>> part2Map, Set<Long> upsertedIds) {
        Map<Long, KospiFinancialInfo> financialInfoMap = kospiFinancialInfoRepository.findAllByKospiMasterIdIn(upsertedIds)
                .stream()
                .collect(Collectors.toMap(KospiFinancialInfo::getKospiMasterId, Function.identity()));

        List<KospiFinancialInfo> updated = updatedPart1s
                .stream()
                .map(part1 -> {
                    Map<String, String> part2Info = part2Map.get(part1.getShortCode());
                    KospiFinancialInfo old = financialInfoMap.get(part1.getId());

                    if (old == null) {
                        return new KospiFinancialInfo(
                                part1.getId(),
                                TypeConverter.toInt(part2Info.get(KospiDetailInfo.SALES.getValue())),
                                TypeConverter.toInt(part2Info.get(KospiDetailInfo.OPERATING_PROFIT.getValue())),
                                TypeConverter.toInt(part2Info.get(KospiDetailInfo.ORDINARY_PROFIT.getValue())),
                                TypeConverter.toInt(part2Info.get(KospiDetailInfo.NET_INCOME.getValue())),
                                TypeConverter.toDouble(part2Info.get(KospiDetailInfo.ROE.getValue())),
                                TypeConverter.toDateTime(part2Info.get(KospiDetailInfo.REFERENCE_YEAR_MONTH.getValue())),
                                TypeConverter.toInt(part2Info.get(KospiDetailInfo.MARKET_CAP.getValue()))
                        );
                    }

                    return old.update(
                            TypeConverter.toInt(part2Info.get(KospiDetailInfo.SALES.getValue())),
                            TypeConverter.toInt(part2Info.get(KospiDetailInfo.OPERATING_PROFIT.getValue())),
                            TypeConverter.toInt(part2Info.get(KospiDetailInfo.ORDINARY_PROFIT.getValue())),
                            TypeConverter.toInt(part2Info.get(KospiDetailInfo.NET_INCOME.getValue())),
                            TypeConverter.toDouble(part2Info.get(KospiDetailInfo.ROE.getValue())),
                            TypeConverter.toDateTime(part2Info.get(KospiDetailInfo.REFERENCE_YEAR_MONTH.getValue())),
                            TypeConverter.toInt(part2Info.get(KospiDetailInfo.MARKET_CAP.getValue()))
                    );
                })
                .toList();

        kospiFinancialInfoRepository.saveAll(updated);
    }
}
