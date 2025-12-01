package com.simudap.service.kospi;

import com.simudap.enums.stock_info.KospiDetailInfo;
import com.simudap.model.kospi.KospiListingBasicInfo;
import com.simudap.model.kospi.KospiMaster;
import com.simudap.repository.kospi.KospiListingBasicInfoRepository;
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
public class KospiListingBasicInfoService {
    private final KospiListingBasicInfoRepository kospiListingBasicInfoRepository;

    public void upsert(List<KospiMaster> updatedPart1s, Map<String, Map<String, String>> part2Map, Set<Long> upsertedIds) {
        Map<Long, KospiListingBasicInfo> listingBasicInfoMap = kospiListingBasicInfoRepository.findAllByKospiMasterIdIn(upsertedIds)
                .stream()
                .collect(Collectors.toMap(KospiListingBasicInfo::getKospiMasterId, Function.identity()));

        List<KospiListingBasicInfo> updated = updatedPart1s
                .stream()
                .map(part1 -> {
                    Map<String, String> part2Info = part2Map.get(part1.getShortCode());
                    KospiListingBasicInfo old = listingBasicInfoMap.get(part1.getId());

                    if (old == null) {
                        return new KospiListingBasicInfo(
                                part1.getId(),
                                TypeConverter.toInt(part2Info.get(KospiDetailInfo.PAR_VALUE.getValue())),
                                TypeConverter.toDateTime(part2Info.get(KospiDetailInfo.LISTING_DATE.getValue())),
                                TypeConverter.toInt(part2Info.get(KospiDetailInfo.LISTED_SHARES.getValue())),
                                TypeConverter.toLong(part2Info.get(KospiDetailInfo.CAPITAL.getValue())),
                                TypeConverter.toInt(part2Info.get(KospiDetailInfo.FISCAL_MONTH.getValue())),
                                TypeConverter.toInt(part2Info.get(KospiDetailInfo.IPO_PRICE.getValue())),
                                part2Info.get(KospiDetailInfo.GROUP_COMPANY_CODE.getValue())
                        );
                    }

                    return old.update(
                            TypeConverter.toInt(part2Info.get(KospiDetailInfo.PAR_VALUE.getValue())),
                            TypeConverter.toDateTime(part2Info.get(KospiDetailInfo.LISTING_DATE.getValue())),
                            TypeConverter.toInt(part2Info.get(KospiDetailInfo.LISTED_SHARES.getValue())),
                            TypeConverter.toLong(part2Info.get(KospiDetailInfo.CAPITAL.getValue())),
                            TypeConverter.toInt(part2Info.get(KospiDetailInfo.FISCAL_MONTH.getValue())),
                            TypeConverter.toInt(part2Info.get(KospiDetailInfo.IPO_PRICE.getValue())),
                            part2Info.get(KospiDetailInfo.GROUP_COMPANY_CODE.getValue())
                    );
                })
                .toList();

        kospiListingBasicInfoRepository.saveAll(updated);
    }
}
