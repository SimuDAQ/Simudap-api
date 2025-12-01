package com.simudap.service.kospi;

import com.simudap.enums.stock_info.KospiDetailInfo;
import com.simudap.model.kospi.KospiIndustryInfo;
import com.simudap.model.kospi.KospiMaster;
import com.simudap.repository.kospi.KospiIndustryInfoRepository;
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
public class KospiIndustryInfoService {
    private final KospiIndustryInfoRepository kospiIndustryInfoRepository;

    public void upsert(List<KospiMaster> updatedPart1s, Map<String, Map<String, String>> part2Map, Set<Long> upsertedIds) {
        Map<Long, KospiIndustryInfo> industryInfoMap = kospiIndustryInfoRepository.findAllByKospiMasterIdIn(upsertedIds)
                .stream()
                .collect(Collectors.toMap(KospiIndustryInfo::getKospiMasterId, Function.identity()));

        List<KospiIndustryInfo> updated = updatedPart1s
                .stream()
                .map(part1 -> {
                    Map<String, String> part2Info = part2Map.get(part1.getShortCode());
                    KospiIndustryInfo old = industryInfoMap.get(part1.getId());

                    if (old == null) {
                        return new KospiIndustryInfo(
                                part1.getId(),
                                TypeConverter.toInt(part2Info.get(KospiDetailInfo.INDEX_INDUSTRY_LARGE.getValue())),
                                TypeConverter.toInt(part2Info.get(KospiDetailInfo.INDEX_INDUSTRY_MID.getValue())),
                                TypeConverter.toInt(part2Info.get(KospiDetailInfo.INDEX_INDUSTRY_SMALL.getValue()))
                        );
                    }

                    return old.update(
                            TypeConverter.toInt(part2Info.get(KospiDetailInfo.INDEX_INDUSTRY_LARGE.getValue())),
                            TypeConverter.toInt(part2Info.get(KospiDetailInfo.INDEX_INDUSTRY_MID.getValue())),
                            TypeConverter.toInt(part2Info.get(KospiDetailInfo.INDEX_INDUSTRY_SMALL.getValue()))
                    );
                })
                .toList();

        kospiIndustryInfoRepository.saveAll(updated);
    }
}
