package com.simudap.service.kospi;

import com.simudap.enums.stock_info.KospiDetailInfo;
import com.simudap.model.kospi.KospiMaster;
import com.simudap.model.kospi.KospiTypeInfo;
import com.simudap.repository.kospi.KospiTypeInfoRepository;
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
public class KospiTypeInfoService {
    private final KospiTypeInfoRepository kospiTypeInfoRepository;

    public void upsert(List<KospiMaster> updatedPart1s, Map<String, Map<String, String>> part2Map, Set<Long> upsertedIds) {
        Map<Long, KospiTypeInfo> typeInfoMap = kospiTypeInfoRepository.findAllByKospiMasterIdIn(upsertedIds)
                .stream()
                .collect(Collectors.toMap(KospiTypeInfo::getKospiMasterId, Function.identity()));

        List<KospiTypeInfo> updated = updatedPart1s
                .stream()
                .map(part1 -> {
                    Map<String, String> kospiInfo = part2Map.get(part1.getShortCode());
                    KospiTypeInfo old = typeInfoMap.get(part1.getId());

                    if (old == null) {
                        return new KospiTypeInfo(
                                part1.getId(),
                                kospiInfo.get(KospiDetailInfo.GROUP_CODE.getValue()),
                                TypeConverter.toInt(kospiInfo.get(KospiDetailInfo.MARKET_CAP_SIZE.getValue())),
                                kospiInfo.get(KospiDetailInfo.MANUFACTURING.getValue()),
                                TypeConverter.toInt(kospiInfo.get(KospiDetailInfo.ETP.getValue())),
                                kospiInfo.get(KospiDetailInfo.SPAC.getValue()),
                                kospiInfo.get(KospiDetailInfo.ELW_ISSUED.getValue())
                        );
                    }

                    return old.update(
                            kospiInfo.get(KospiDetailInfo.GROUP_CODE.getValue()),
                            TypeConverter.toInt(kospiInfo.get(KospiDetailInfo.MARKET_CAP_SIZE.getValue())),
                            kospiInfo.get(KospiDetailInfo.MANUFACTURING.getValue()),
                            TypeConverter.toInt(kospiInfo.get(KospiDetailInfo.ETP.getValue())),
                            kospiInfo.get(KospiDetailInfo.SPAC.getValue()),
                            kospiInfo.get(KospiDetailInfo.ELW_ISSUED.getValue())
                    );
                })
                .toList();

        kospiTypeInfoRepository.saveAll(updated);
    }
}
