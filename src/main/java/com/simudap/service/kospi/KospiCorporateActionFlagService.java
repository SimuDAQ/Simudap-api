package com.simudap.service.kospi;

import com.simudap.enums.stock_info.KospiDetailInfo;
import com.simudap.model.kospi.KospiCorporateActionFlag;
import com.simudap.model.kospi.KospiMaster;
import com.simudap.repository.kospi.KospiCorporateActionFlagRepository;
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
public class KospiCorporateActionFlagService {
    private final KospiCorporateActionFlagRepository kospiCorporateActionFlagRepository;

    public void upsert(List<KospiMaster> updatedPart1s, Map<String, Map<String, String>> part2Map, Set<Long> upsertedIds) {
        Map<Long, KospiCorporateActionFlag> coporateActionFlagMap = kospiCorporateActionFlagRepository.findAllByKospiMasterIdIn(upsertedIds)
                .stream()
                .collect(Collectors.toMap(KospiCorporateActionFlag::getKospiMasterId, Function.identity()));

        List<KospiCorporateActionFlag> updated = updatedPart1s
                .stream()
                .map(part1 -> {
                    Map<String, String> part2Info = part2Map.get(part1.getShortCode());
                    KospiCorporateActionFlag old = coporateActionFlagMap.get(part1.getId());

                    if (old == null) {
                        return new KospiCorporateActionFlag(
                                part1.getId(),
                                TypeConverter.toInt(part2Info.get(KospiDetailInfo.LOCK_TYPE.getValue())),
                                TypeConverter.toInt(part2Info.get(KospiDetailInfo.PAR_VALUE_CHANGE.getValue())),
                                TypeConverter.toInt(part2Info.get(KospiDetailInfo.CAPITAL_INCREASE_TYPE.getValue())),
                                TypeConverter.toInt(part2Info.get(KospiDetailInfo.PREFERRED_STOCK.getValue()))
                        );
                    }

                    return old.update(
                            TypeConverter.toInt(part2Info.get(KospiDetailInfo.LOCK_TYPE.getValue())),
                            TypeConverter.toInt(part2Info.get(KospiDetailInfo.PAR_VALUE_CHANGE.getValue())),
                            TypeConverter.toInt(part2Info.get(KospiDetailInfo.CAPITAL_INCREASE_TYPE.getValue())),
                            TypeConverter.toInt(part2Info.get(KospiDetailInfo.PREFERRED_STOCK.getValue()))
                    );
                })
                .toList();

        kospiCorporateActionFlagRepository.saveAll(updated);
    }
}
