package com.simudap.service.kospi;

import com.simudap.dto.stock.StockBaseInfo;
import com.simudap.model.kospi.KospiMaster;
import com.simudap.repository.kospi.KospiMasterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KospiMasterService {
    private final KospiMasterRepository kospiMasterRepository;

    // KOSPI 에 신규 상장된 주식 또는 새로 업데이트된 정보 업데이트
    public List<KospiMaster> upsert(Map<String, StockBaseInfo> part1) {
        Map<String, KospiMaster> oldInfos = findAll();

        List<KospiMaster> updated = part1.values()
                .stream()
                .map(info -> {
                    KospiMaster old = oldInfos.get(info.shortCode());
                    if (old == null) {
                        return new KospiMaster(info.shortCode(), info.standardCode(), info.nameKr(), false);
                    }
                    return old.update(info.shortCode(), info.standardCode(), info.nameKr(), false);
                })
                .toList();

        return kospiMasterRepository.saveAll(updated);
    }

    // 상장폐지된 주식 DB 에서 제거
    public void removeDeListed(Map<String, StockBaseInfo> updatedInfos) {
        Map<String, KospiMaster> oldInfos = findAll();

        for (KospiMaster old : oldInfos.values()) {
            if (updatedInfos.get(old.getShortCode()) == null) {
                old.updateStatus(true);
            }
        }
    }

    private Map<String, KospiMaster> findAll() {
        return kospiMasterRepository.findAll()
                .stream()
                .collect(Collectors.toMap(KospiMaster::getShortCode, Function.identity()));
    }
}
