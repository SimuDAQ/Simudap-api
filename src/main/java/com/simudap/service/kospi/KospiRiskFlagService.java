package com.simudap.service.kospi;

import com.simudap.enums.stock_info.KospiDetailInfo;
import com.simudap.model.kospi.KospiMaster;
import com.simudap.model.kospi.KospiRiskFlag;
import com.simudap.repository.kospi.KospiRiskFlagRepository;
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
public class KospiRiskFlagService {
    private final KospiRiskFlagRepository kospiRiskFlagRepository;

    public void upsert(List<KospiMaster> updatedPart1s, Map<String, Map<String, String>> part2Map, Set<Long> upsertedIds) {
        Map<Long, KospiRiskFlag> riskFlagMap = kospiRiskFlagRepository.findAllByKospiMasterIdIn(upsertedIds)
                .stream()
                .collect(Collectors.toMap(KospiRiskFlag::getKospiMasterId, Function.identity()));

        List<KospiRiskFlag> updated = updatedPart1s
                .stream()
                .map(part1 -> {
                    Map<String, String> part2Info = part2Map.get(part1.getShortCode());
                    KospiRiskFlag old = riskFlagMap.get(part1.getId());

                    if (old == null) {
                        return new KospiRiskFlag(
                                part1.getId(),
                                part2Info.get(KospiDetailInfo.LOW_LIQUIDITY.getValue()),
                                TypeConverter.toInt(part2Info.get(KospiDetailInfo.SHORT_TERM_OVERHEAT.getValue())),
                                TypeConverter.toInt(part2Info.get(KospiDetailInfo.MARKET_WARNING.getValue())),
                                part2Info.get(KospiDetailInfo.WARNING_NOTICE.getValue()),
                                part2Info.get(KospiDetailInfo.SHORT_SELL_OVERHEAT.getValue()),
                                part2Info.get(KospiDetailInfo.ABNORMAL_RISE.getValue())
                        );
                    }

                    return old.update(
                            part2Info.get(KospiDetailInfo.LOW_LIQUIDITY.getValue()),
                            TypeConverter.toInt(part2Info.get(KospiDetailInfo.SHORT_TERM_OVERHEAT.getValue())),
                            TypeConverter.toInt(part2Info.get(KospiDetailInfo.MARKET_WARNING.getValue())),
                            part2Info.get(KospiDetailInfo.WARNING_NOTICE.getValue()),
                            part2Info.get(KospiDetailInfo.SHORT_SELL_OVERHEAT.getValue()),
                            part2Info.get(KospiDetailInfo.ABNORMAL_RISE.getValue())
                    );
                })
                .toList();

        kospiRiskFlagRepository.saveAll(updated);
    }
}
