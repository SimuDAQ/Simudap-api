package com.simudap.service;

import com.simudap.dto.stock.StockBaseInfo;
import com.simudap.dto.stock.StockInfoResponse;
import com.simudap.enums.stock_info.StockDetailInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipInputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockInfoDownloadService {

    private final RestTemplate restTemplate;

    public StockInfoResponse downloadFile(StockDetailInfo detailInfo, int part2Length, String downloadUrl) {
        // key : shortCode, value : part1 에 저장될 데이터
        Map<String, StockBaseInfo> part1Map = new HashMap<>();
        // key : shortCode, key : field 이름 , value : 실질적인 값
        Map<String, Map<String, String>> part2Map = new HashMap<>();

        ResponseEntity<byte[]> response = restTemplate.getForEntity(downloadUrl, byte[].class);
        byte[] zipBytes = response.getBody();
        Charset charset = Charset.forName("CP949");

        try (ByteArrayInputStream bais = new ByteArrayInputStream(zipBytes);
             ZipInputStream zis = new ZipInputStream(bais)) {

            if (zis.getNextEntry() == null) {
                throw new FileNotFoundException("Zip File Not Found");
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(zis, charset));
            String line;

            while ((line = reader.readLine()) != null) {
                int totalLength = line.length();
                int part2Start = totalLength - part2Length;

                String part1 = line.substring(0, part2Start);
                String part2 = line.substring(part2Start);

                // part1 parsing
                String shortCode = part1.substring(0, 9).trim();
                String standardCode = part1.substring(9, 21).trim();
                String nameKr = part1.substring(21).trim();

                StockBaseInfo stockInfo = new StockBaseInfo(shortCode, standardCode, nameKr);
                part1Map.put(stockInfo.shortCode(), stockInfo);

                // part2 parsing
                int index = 0;
                part2Map.put(shortCode, new HashMap<>());
                Map<String, String> detailMap = part2Map.get(shortCode);
                Map<Integer, StockDetailInfo> detailInfoMap = detailInfo.convertToMap();

                for (int i = 0; i < detailInfoMap.size(); i++) {
                    StockDetailInfo info = detailInfoMap.get(i);
                    String value = part2.substring(index, index + info.getFieldSpace());
                    detailMap.put(info.getValue(), value);
                    index += info.getFieldSpace();
                }
            }

            zis.closeEntry();

            return new StockInfoResponse(part1Map, part2Map);
        } catch (Exception e) {
            throw new RuntimeException("Error occurred when processing of updating KOSPI file" + e.getMessage());
        }
    }
}
