package com.simudap.service.kospi;

import com.simudap.enums.stock_info.KospiDetailInfo;
import com.simudap.model.kospi.KospiIndexInfo;
import com.simudap.model.kospi.KospiMaster;
import com.simudap.repository.kospi.KospiIndexInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KospiIndexInfoService {
    private final KospiIndexInfoRepository kospiIndexInfoRepository;

    public void upsert(List<KospiMaster> updatedPart1s, Map<String, Map<String, String>> part2Map, Set<Long> upsertedIds) {
        Map<Long, KospiIndexInfo> indexInfoMap = kospiIndexInfoRepository.findAllByKospiMasterIdIn(upsertedIds)
                .stream()
                .collect(Collectors.toMap(KospiIndexInfo::getKospiMasterId, Function.identity()));

        List<KospiIndexInfo> updated = updatedPart1s
                .stream()
                .map(part1 -> {
                    Map<String, String> part2Info = part2Map.get(part1.getShortCode());
                    KospiIndexInfo old = indexInfoMap.get(part1.getId());

                    if (old == null) {
                        return new KospiIndexInfo(
                                part1.getId(),
                                part2Info.get(KospiDetailInfo.KOSPI100.getValue()),
                                part2Info.get(KospiDetailInfo.KOSPI50.getValue()),
                                part2Info.get(KospiDetailInfo.KRX.getValue()),
                                part2Info.get(KospiDetailInfo.KRX100.getValue()),
                                part2Info.get(KospiDetailInfo.SRI.getValue()),
                                part2Info.get(KospiDetailInfo.KRX300.getValue()),
                                part2Info.get(KospiDetailInfo.KOSPI.getValue())
                        );
                    }

                    return old.update(
                            part2Info.get(KospiDetailInfo.KOSPI100.getValue()),
                            part2Info.get(KospiDetailInfo.KOSPI50.getValue()),
                            part2Info.get(KospiDetailInfo.KRX.getValue()),
                            part2Info.get(KospiDetailInfo.KRX100.getValue()),
                            part2Info.get(KospiDetailInfo.SRI.getValue()),
                            part2Info.get(KospiDetailInfo.KRX300.getValue()),
                            part2Info.get(KospiDetailInfo.KOSPI.getValue())
                    );
                })
                .toList();

        kospiIndexInfoRepository.saveAll(updated);
    }
}
