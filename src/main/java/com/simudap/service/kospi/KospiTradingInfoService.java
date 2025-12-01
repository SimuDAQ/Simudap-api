package com.simudap.service.kospi;

import com.simudap.enums.stock_info.KospiDetailInfo;
import com.simudap.model.kospi.KospiMaster;
import com.simudap.model.kospi.KospiTradingInfo;
import com.simudap.repository.kospi.KospiTradingInfoRepository;
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
public class KospiTradingInfoService {
    private final KospiTradingInfoRepository kospiTradingInfoRepository;

    public void upsert(List<KospiMaster> updatedPart1s, Map<String, Map<String, String>> part2Map, Set<Long> upsertedIds) {
        Map<Long, KospiTradingInfo> tradingInfoMap = kospiTradingInfoRepository.findAllByKospiMasterIdIn(upsertedIds)
                .stream()
                .collect(Collectors.toMap(KospiTradingInfo::getKospiMasterId, Function.identity()));

        List<KospiTradingInfo> updated = updatedPart1s
                .stream()
                .map(part1 -> {
                    Map<String, String> part2Info = part2Map.get(part1.getShortCode());
                    KospiTradingInfo old = tradingInfoMap.get(part1.getId());

                    if (old == null) {
                        return new KospiTradingInfo(
                                part1.getId(),
                                TypeConverter.toInt(part2Info.get(KospiDetailInfo.BASE_PRICE.getValue())),
                                TypeConverter.toInt(part2Info.get(KospiDetailInfo.TRADE_UNIT.getValue())),
                                TypeConverter.toInt(part2Info.get(KospiDetailInfo.AFTER_HOURS_UNIT.getValue())),
                                TypeConverter.toInt(part2Info.get(KospiDetailInfo.MARGIN_RATE.getValue())),
                                part2Info.get(KospiDetailInfo.CREDIT_AVAILABLE.getValue()),
                                TypeConverter.toInt(part2Info.get(KospiDetailInfo.CREDIT_DAYS.getValue())),
                                TypeConverter.toInt(part2Info.get(KospiDetailInfo.PREVIOUS_DAY_VOLUME.getValue())),
                                part2Info.get(KospiDetailInfo.CREDIT_LIMIT_OVER.getValue()),
                                part2Info.get(KospiDetailInfo.COLLATERAL_LOAN_AVAILABLE.getValue()),
                                part2Info.get(KospiDetailInfo.STOCK_LENDING_AVAILABLE.getValue())
                        );
                    }

                    return old.update(
                            TypeConverter.toInt(part2Info.get(KospiDetailInfo.BASE_PRICE.getValue())),
                            TypeConverter.toInt(part2Info.get(KospiDetailInfo.TRADE_UNIT.getValue())),
                            TypeConverter.toInt(part2Info.get(KospiDetailInfo.AFTER_HOURS_UNIT.getValue())),
                            TypeConverter.toInt(part2Info.get(KospiDetailInfo.MARGIN_RATE.getValue())),
                            part2Info.get(KospiDetailInfo.CREDIT_AVAILABLE.getValue()),
                            TypeConverter.toInt(part2Info.get(KospiDetailInfo.CREDIT_DAYS.getValue())),
                            TypeConverter.toInt(part2Info.get(KospiDetailInfo.PREVIOUS_DAY_VOLUME.getValue())),
                            part2Info.get(KospiDetailInfo.CREDIT_LIMIT_OVER.getValue()),
                            part2Info.get(KospiDetailInfo.COLLATERAL_LOAN_AVAILABLE.getValue()),
                            part2Info.get(KospiDetailInfo.STOCK_LENDING_AVAILABLE.getValue())
                    );
                })
                .toList();

        kospiTradingInfoRepository.saveAll(updated);
    }
}
