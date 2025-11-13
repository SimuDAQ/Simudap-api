package com.simudap.service;

import com.simudap.model.KospiMaster;
import com.simudap.repository.KospiMasterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.ZipInputStream;

@Service
@RequiredArgsConstructor
public class SchedulerService {
    private static final String KOSPI_URL = "https://new.real.download.dws.co.kr/common/master/kospi_code.mst.zip";

    private final KospiMasterRepository kospiMasterRepository;
    private final RestTemplate restTemplate;

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void downloadKospiFile() {
        Map<String, KospiInfo> updatedInfos = new HashMap<>();

        ResponseEntity<byte[]> response = restTemplate.getForEntity(KOSPI_URL, byte[].class);
        byte[] zipBytes = response.getBody();
        Charset charset = Charset.forName("CP949");

        try(ByteArrayInputStream bais = new ByteArrayInputStream(zipBytes);
            ZipInputStream zis = new ZipInputStream(bais)) {

            while(zis.getNextEntry() != null) {
                BufferedReader br = new BufferedReader(new InputStreamReader(zis, charset));
                String line;

                while((line = br.readLine()) != null) {
                    int len = line.length();
                    int part2Len = 228;

                    String part1 = line.substring(0, len - part2Len);

                    String shortCode = part1.substring(0,9).trim();
                    String standardCode = part1.substring(9,21).trim();
                    String nameKr = part1.substring(21).trim();

                    KospiInfo kospiInfo = new KospiInfo(shortCode, standardCode, nameKr);
                    updatedInfos.put(kospiInfo.shortCode(), kospiInfo);
                }
            }

            zis.closeEntry();
        } catch (Exception e) {
            throw new RuntimeException("Error occurred when processing of updating KOSPI file" + e.getMessage());
        }

        Map<String, KospiMaster> olds = kospiMasterRepository.findAll()
                .stream()
                .collect(Collectors.toMap(KospiMaster::getShortCode, Function.identity()));

        upsert(olds, updatedInfos);
        removeDeListed(olds, updatedInfos);
    }

    // KOSPI 에 신규 상장된 주식 또는 새로 업데이트된 정보 업데이트
    private void upsert(Map<String, KospiMaster> oldInfos, Map<String, KospiInfo> updatedInfos) {
        List<KospiMaster> updated = updatedInfos.values()
                .stream()
                .map(info -> {
                    KospiMaster old = oldInfos.get(info.shortCode());
                    if (old == null) {
                        return new KospiMaster(info.shortCode(), info.standardCode(), info.nameKr());
                    }

                    return old.update(info.shortCode(), info.standardCode(), info.nameKr());
                })
                .toList();

        kospiMasterRepository.saveAll(updated);
    }

    // 상장폐지된 주식 DB 에서 제거
    private void removeDeListed(Map<String, KospiMaster> oldInfos, Map<String, KospiInfo> updatedInfos) {
        Set<Long> deListedIds = oldInfos.values()
                .stream()
                .filter(old -> updatedInfos.get(old.getShortCode()) == null)
                .map(KospiMaster::getId)
                .collect(Collectors.toSet());

        kospiMasterRepository.deleteByIds(deListedIds);
    }

    private record KospiInfo(
            String shortCode,
            String standardCode,
            String nameKr
    ) {}
}
