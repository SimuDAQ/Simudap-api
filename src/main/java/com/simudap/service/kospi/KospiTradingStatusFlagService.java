package com.simudap.service.kospi;

import com.simudap.enums.stock_info.KospiDetailInfo;
import com.simudap.model.kospi.KospiMaster;
import com.simudap.model.kospi.KospiTradingStatusFlag;
import com.simudap.repository.kospi.KospiTradingStatusFlagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KospiTradingStatusFlagService {
    private final KospiTradingStatusFlagRepository kospiTradingStatusFlagRepository;

    public void upsert(List<KospiMaster> updatedPart1s, Map<String, Map<String, String>> part2Map, Set<Long> upsertedIds) {
        Map<Long, KospiTradingStatusFlag> kospiTradingStatusFlagMap = kospiTradingStatusFlagRepository.findAllByKospiMasterIdIn(upsertedIds)
                .stream()
                .collect(Collectors.toMap(KospiTradingStatusFlag::getKospiMasterId, Function.identity()));

        List<KospiTradingStatusFlag> updated = updatedPart1s
                .stream()
                .map(part1 -> {
                    Map<String, String> part2Info = part2Map.get(part1.getShortCode());
                    KospiTradingStatusFlag old = kospiTradingStatusFlagMap.get(part1.getId());

                    if (old == null) {
                        return new KospiTradingStatusFlag(
                                part1.getId(),
                                part2Info.get(KospiDetailInfo.TRADING_HALT.getValue()),
                                part2Info.get(KospiDetailInfo.LIQUIDATION.getValue()),
                                part2Info.get(KospiDetailInfo.MANAGEMENT_ISSUE.getValue()),
                                part2Info.get(KospiDetailInfo.GOVERNANCE_INDEX.getValue()),
                                part2Info.get(KospiDetailInfo.POOR_DISCLOSURE.getValue()),
                                part2Info.get(KospiDetailInfo.BACKDOOR_LISTING.getValue())
                        );
                    }

                    return old.update(
                            part2Info.get(KospiDetailInfo.TRADING_HALT.getValue()),
                            part2Info.get(KospiDetailInfo.LIQUIDATION.getValue()),
                            part2Info.get(KospiDetailInfo.MANAGEMENT_ISSUE.getValue()),
                            part2Info.get(KospiDetailInfo.GOVERNANCE_INDEX.getValue()),
                            part2Info.get(KospiDetailInfo.POOR_DISCLOSURE.getValue()),
                            part2Info.get(KospiDetailInfo.BACKDOOR_LISTING.getValue())
                    );
                })
                .toList();

        kospiTradingStatusFlagRepository.saveAll(updated);
    }
}
