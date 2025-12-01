package com.simudap.service.kospi;

import com.simudap.enums.stock_info.KospiDetailInfo;
import com.simudap.model.kospi.KospiMaster;
import com.simudap.model.kospi.KospiSectorInfo;
import com.simudap.repository.kospi.KospiSectorInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KospiSectorInfoService {
    private final KospiSectorInfoRepository kospiSectorInfoRepository;

    public void upsert(List<KospiMaster> updatedPart1s, Map<String, Map<String, String>> part2Map, Set<Long> upsertedIds) {
        Map<Long, KospiSectorInfo> sectorMap = kospiSectorInfoRepository.findAllByKospiMasterIdIn(upsertedIds)
                .stream()
                .collect(Collectors.toMap(KospiSectorInfo::getKospiMasterId, Function.identity()));

        List<KospiSectorInfo> updated = updatedPart1s
                .stream()
                .map(part1 -> {
                    Map<String, String> part2Info = part2Map.get(part1.getShortCode());
                    KospiSectorInfo old = sectorMap.get(part1.getId());

                    if (old == null) {
                        return new KospiSectorInfo(
                                part1.getId(),
                                part2Info.get(KospiDetailInfo.KOSPI200_SECTOR.getValue()),
                                part2Info.get(KospiDetailInfo.KRX_CAR.getValue()),
                                part2Info.get(KospiDetailInfo.KRX_SEMICONDUCTOR.getValue()),
                                part2Info.get(KospiDetailInfo.KRX_BIO.getValue()),
                                part2Info.get(KospiDetailInfo.KRX_BANK.getValue()),
                                part2Info.get(KospiDetailInfo.KRX_ENERGY_CHEM.getValue()),
                                part2Info.get(KospiDetailInfo.KRX_STEEL.getValue()),
                                part2Info.get(KospiDetailInfo.KRX_MEDIA_COMM.getValue()),
                                part2Info.get(KospiDetailInfo.KRX_CONSTRUCTION.getValue()),
                                part2Info.get(KospiDetailInfo.KRX_SECURITIES.getValue()),
                                part2Info.get(KospiDetailInfo.KRX_SHIP.getValue()),
                                part2Info.get(KospiDetailInfo.KRX_SECTOR_INSURANCE.getValue()),
                                part2Info.get(KospiDetailInfo.KRX_SECTOR_TRANSPORT.getValue())
                        );
                    }

                    return old.update(
                            part2Info.get(KospiDetailInfo.KOSPI200_SECTOR.getValue()),
                            part2Info.get(KospiDetailInfo.KRX_CAR.getValue()),
                            part2Info.get(KospiDetailInfo.KRX_SEMICONDUCTOR.getValue()),
                            part2Info.get(KospiDetailInfo.KRX_BIO.getValue()),
                            part2Info.get(KospiDetailInfo.KRX_BANK.getValue()),
                            part2Info.get(KospiDetailInfo.KRX_ENERGY_CHEM.getValue()),
                            part2Info.get(KospiDetailInfo.KRX_STEEL.getValue()),
                            part2Info.get(KospiDetailInfo.KRX_MEDIA_COMM.getValue()),
                            part2Info.get(KospiDetailInfo.KRX_CONSTRUCTION.getValue()),
                            part2Info.get(KospiDetailInfo.KRX_SECURITIES.getValue()),
                            part2Info.get(KospiDetailInfo.KRX_SHIP.getValue()),
                            part2Info.get(KospiDetailInfo.KRX_SECTOR_INSURANCE.getValue()),
                            part2Info.get(KospiDetailInfo.KRX_SECTOR_TRANSPORT.getValue())
                    );
                })
                .toList();

        kospiSectorInfoRepository.saveAll(updated);
    }
}
