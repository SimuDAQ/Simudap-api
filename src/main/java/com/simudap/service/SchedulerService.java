package com.simudap.service;

import com.simudap.common.Stock;
import com.simudap.model.*;
import com.simudap.repository.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.ZipInputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class SchedulerService {
    private static final String KOSPI_URL = "https://new.real.download.dws.co.kr/common/master/kospi_code.mst.zip";

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final KospiMasterRepository kospiMasterRepository;
    private final KospiTypeInfoRepository kospiTypeInfoRepository;
    private final KospiTradingInfoRepository kospiTradingInfoRepository;
    private final KospiTradingStatusFlagRepository kospiTradingStatusFlagRepository;
    private final KospiSectorInfoRepository kospiSectorInfoRepository;
    private final KospiRiskFlagRepository kospiRiskFlagRepository;
    private final KospiListingBasicInfoRepository kospiListingBasicInfoRepository;
    private final KospiIndustryInfoRepository kospiIndustryInfoRepository;
    private final KospiIndexInfoRepository kospiIndexInfoRepository;
    private final KospiFinancialInfoRepository kospiFinancialInfoRepository;
    private final KospiCorporateActionFlagRepository kospiCorporateActionFlagRepository;
    private final RestTemplate restTemplate;

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void downloadKospiFile() {
        Map<String, KospiInfo> updatedInfos = new HashMap<>();

        int length = "BC 000000000000NN 0NNN NN    N  0  N     0000009500000100000NNN00NNN000000100N09000000000000000000000100020250418000000000021941000000000021941377000         0 NNN00000000000000000000000000000000000000.00        000000208   NNN".length();
        log.info("길이 = " + length);

        ResponseEntity<byte[]> response = restTemplate.getForEntity(KOSPI_URL, byte[].class);
        byte[] zipBytes = response.getBody();
        Charset charset = Charset.forName("CP949");

        try (ByteArrayInputStream bais = new ByteArrayInputStream(zipBytes);
             ZipInputStream zis = new ZipInputStream(bais)) {

            while (zis.getNextEntry() != null) {
                BufferedReader br = new BufferedReader(new InputStreamReader(zis, charset));
                String line;

                while ((line = br.readLine()) != null) {
                    int len = line.length();
                    int part2Len = 227;

                    String part1 = line.substring(0, len - part2Len);
                    String part2 = line.substring(len - part2Len);

                    Map<String, String> part2Map = new HashMap<>();
                    int index = 0;

                    Stock[] stock = Stock.values();

                    for(int i = 0; i < stock.length; i++) {
                        String substring = part2.substring(index, index + stock[i].getFieldSpace());
                        part2Map.put(stock[i].getValue(), substring);
                        index += stock[i].getFieldSpace();
                    }

                    String shortCode = part1.substring(0, 9).trim();
                    String standardCode = part1.substring(9, 21).trim();
                    String nameKr = part1.substring(21).trim();

                    KospiInfo kospiInfo = new KospiInfo(shortCode, standardCode, nameKr, part2Map);
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

        List<KospiMaster> upserted = upsertPart1(olds, updatedInfos);

        upsertPart2(updatedInfos, upserted);

        removeDeListed(olds, updatedInfos);
    }

    // KOSPI 에 신규 상장된 주식 또는 새로 업데이트된 정보 업데이트
    private List<KospiMaster> upsertPart1(Map<String, KospiMaster> oldInfos, Map<String, KospiInfo> updatedInfos) {
        List<KospiMaster> updated = updatedInfos.values()
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

    private void upsertPart2(Map<String, KospiInfo> updatedInfos, List<KospiMaster> upserted) {
        Set<Long> upsertedIds = upserted
                .stream()
                .map(KospiMaster::getId)
                .collect(Collectors.toSet());

        upsertTypeInfo(updatedInfos, upserted, upsertedIds);
        upsertTradingStatusFlag(updatedInfos, upserted, upsertedIds);
        upsertTradingInfo(updatedInfos, upserted, upsertedIds);
        upsertSectorInfo(updatedInfos, upserted, upsertedIds);
        upsertRiskFlag(updatedInfos, upserted, upsertedIds);
        upsertListingBasicInfo(updatedInfos, upserted, upsertedIds);
        upsertIndustryInfo(updatedInfos, upserted, upsertedIds);
        upsertIndexInfo(updatedInfos, upserted, upsertedIds);
        upsertFinancialInfo(updatedInfos, upserted, upsertedIds);
        upsertCorporateActionFlag(updatedInfos, upserted, upsertedIds);
    }

    private void upsertCorporateActionFlag(Map<String, KospiInfo> updatedInfos, List<KospiMaster> upserted, Set<Long> upsertedIds) {
        Map<Long, KospiCorporateActionFlag> coporateActionFlagMap = kospiCorporateActionFlagRepository.findAllByKospiMasterIdIn(upsertedIds)
                .stream()
                .collect(Collectors.toMap(KospiCorporateActionFlag::getKospiMasterId, Function.identity()));

        List<KospiCorporateActionFlag> updated = upserted
                .stream()
                .map(kospiMaster -> {
                    KospiInfo kospiInfo = updatedInfos.get(kospiMaster.getShortCode());
                    Map<String, String> part2Map = kospiInfo.part2Map();

                    KospiCorporateActionFlag kospiCorporateActionFlag = coporateActionFlagMap.get(kospiMaster.getId());

                    if (kospiCorporateActionFlag == null) {
                        return new KospiCorporateActionFlag(
                                kospiMaster.getId(),
                                toInt(part2Map.get(Stock.LOCK_TYPE.getValue())),
                                toInt(part2Map.get(Stock.PAR_VALUE_CHANGE.getValue())),
                                toInt(part2Map.get(Stock.CAPITAL_INCREASE_TYPE.getValue())),
                                toInt(part2Map.get(Stock.PREFERRED_STOCK.getValue()))
                        );
                    }

                    return kospiCorporateActionFlag.update(
                            toInt(part2Map.get(Stock.LOCK_TYPE.getValue())),
                            toInt(part2Map.get(Stock.PAR_VALUE_CHANGE.getValue())),
                            toInt(part2Map.get(Stock.CAPITAL_INCREASE_TYPE.getValue())),
                            toInt(part2Map.get(Stock.PREFERRED_STOCK.getValue()))
                    );
                })
                .toList();
        kospiCorporateActionFlagRepository.saveAll(updated);
    }

    private void upsertFinancialInfo(Map<String, KospiInfo> updatedInfos, List<KospiMaster> upserted, Set<Long> upsertedIds) {
        Map<Long, KospiFinancialInfo> financialInfoMap = kospiFinancialInfoRepository.findAllByKospiMasterIdIn(upsertedIds)
                .stream()
                .collect(Collectors.toMap(KospiFinancialInfo::getKospiMasterId, Function.identity()));

        List<KospiFinancialInfo> updated = upserted
                .stream()
                .map(kospiMaster -> {
                    KospiInfo kospiInfo = updatedInfos.get(kospiMaster.getShortCode());
                    Map<String, String> part2Map = kospiInfo.part2Map();

                    KospiFinancialInfo kospiFinancialInfo = financialInfoMap.get(kospiMaster.getId());

                    if (kospiFinancialInfo == null) {
                        return new KospiFinancialInfo(
                                kospiMaster.getId(),
                                toInt(part2Map.get(Stock.SALES.getValue())),
                                toInt(part2Map.get(Stock.OPERATING_PROFIT.getValue())),
                                toInt(part2Map.get(Stock.ORDINARY_PROFIT.getValue())),
                                toInt(part2Map.get(Stock.NET_INCOME.getValue())),
                                toDouble(part2Map.get(Stock.ROE.getValue())),
                                toDateTime(part2Map.get(Stock.REFERENCE_YEAR_MONTH.getValue())),
                                toInt(part2Map.get(Stock.MARKET_CAP.getValue()))
                        );
                    }

                    return kospiFinancialInfo.update(
                            toInt(part2Map.get(Stock.SALES.getValue())),
                            toInt(part2Map.get(Stock.OPERATING_PROFIT.getValue())),
                            toInt(part2Map.get(Stock.ORDINARY_PROFIT.getValue())),
                            toInt(part2Map.get(Stock.NET_INCOME.getValue())),
                            toDouble(part2Map.get(Stock.ROE.getValue())),
                            toDateTime(part2Map.get(Stock.REFERENCE_YEAR_MONTH.getValue())),
                            toInt(part2Map.get(Stock.MARKET_CAP.getValue()))
                    );
                })
                .toList();
        kospiFinancialInfoRepository.saveAll(updated);
    }

    private void upsertIndexInfo(Map<String, KospiInfo> updatedInfos, List<KospiMaster> upserted, Set<Long> upsertedIds) {
        Map<Long, KospiIndexInfo> indexInfoMap = kospiIndexInfoRepository.findAllByKospiMasterIdIn(upsertedIds)
                .stream()
                .collect(Collectors.toMap(KospiIndexInfo::getKospiMasterId, Function.identity()));

        List<KospiIndexInfo> updated = upserted
                .stream()
                .map(kospiMaster -> {
                    KospiInfo kospiInfo = updatedInfos.get(kospiMaster.getShortCode());
                    Map<String, String> part2Map = kospiInfo.part2Map();

                    KospiIndexInfo kospiIndexInfo = indexInfoMap.get(kospiMaster.getId());

                    if (kospiIndexInfo == null) {
                        return new KospiIndexInfo(
                                kospiMaster.getId(),
                                part2Map.get(Stock.KOSPI100.getValue()),
                                part2Map.get(Stock.KOSPI50.getValue()),
                                part2Map.get(Stock.KRX.getValue()),
                                part2Map.get(Stock.KRX100.getValue()),
                                part2Map.get(Stock.SRI.getValue()),
                                part2Map.get(Stock.KRX300.getValue()),
                                part2Map.get(Stock.KOSPI.getValue())
                        );
                    }

                    return kospiIndexInfo.update(
                            part2Map.get(Stock.KOSPI100.getValue()),
                            part2Map.get(Stock.KOSPI50.getValue()),
                            part2Map.get(Stock.KRX.getValue()),
                            part2Map.get(Stock.KRX100.getValue()),
                            part2Map.get(Stock.SRI.getValue()),
                            part2Map.get(Stock.KRX300.getValue()),
                            part2Map.get(Stock.KOSPI.getValue())
                    );
                })
                .toList();
        kospiIndexInfoRepository.saveAll(updated);
    }

    private void upsertIndustryInfo(Map<String, KospiInfo> updatedInfos, List<KospiMaster> upserted, Set<Long> upsertedIds) {
        Map<Long, KospiIndustryInfo> industryInfoMap = kospiIndustryInfoRepository.findAllByKospiMasterIdIn(upsertedIds)
                .stream()
                .collect(Collectors.toMap(KospiIndustryInfo::getKospiMasterId, Function.identity()));

        List<KospiIndustryInfo> updated = upserted
                .stream()
                .map(kospiMaster -> {
                    KospiInfo kospiInfo = updatedInfos.get(kospiMaster.getShortCode());
                    Map<String, String> part2Map = kospiInfo.part2Map();

                    KospiIndustryInfo kospiIndustryInfo = industryInfoMap.get(kospiMaster.getId());

                    if (kospiIndustryInfo == null) {
                        return new KospiIndustryInfo(
                                kospiMaster.getId(),
                                toInt(part2Map.get(Stock.INDEX_INDUSTRY_LARGE.getValue())),
                                toInt(part2Map.get(Stock.INDEX_INDUSTRY_MID.getValue())),
                                toInt(part2Map.get(Stock.INDEX_INDUSTRY_SMALL.getValue()))
                        );
                    }

                    return kospiIndustryInfo.update(
                            toInt(part2Map.get(Stock.INDEX_INDUSTRY_LARGE.getValue())),
                            toInt(part2Map.get(Stock.INDEX_INDUSTRY_MID.getValue())),
                            toInt(part2Map.get(Stock.INDEX_INDUSTRY_SMALL.getValue()))
                    );
                })
                .toList();
        kospiIndustryInfoRepository.saveAll(updated);
    }

    private void upsertListingBasicInfo(Map<String, KospiInfo> updatedInfos, List<KospiMaster> upserted, Set<Long> upsertedIds) {
        Map<Long, KospiListingBasicInfo> listingBasicInfoMap = kospiListingBasicInfoRepository.findAllByKospiMasterIdIn(upsertedIds)
                .stream()
                .collect(Collectors.toMap(KospiListingBasicInfo::getKospiMasterId, Function.identity()));

        List<KospiListingBasicInfo> updated = upserted
                .stream()
                .map(kospiMaster -> {
                    KospiInfo kospiInfo = updatedInfos.get(kospiMaster.getShortCode());
                    Map<String, String> part2Map = kospiInfo.part2Map();

                    KospiListingBasicInfo kospiListingBasicInfo = listingBasicInfoMap.get(kospiMaster.getId());

                    if (kospiListingBasicInfo == null) {
                        return new KospiListingBasicInfo(
                                kospiMaster.getId(),
                                toInt(part2Map.get(Stock.PAR_VALUE.getValue())),
                                toDateTime(part2Map.get(Stock.LISTING_DATE.getValue())),
                                toInt(part2Map.get(Stock.LISTED_SHARES.getValue())),
                                toLong(part2Map.get(Stock.CAPITAL.getValue())),
                                toInt(part2Map.get(Stock.FISCAL_MONTH.getValue())),
                                toInt(part2Map.get(Stock.IPO_PRICE.getValue())),
                                part2Map.get(Stock.GROUP_COMPANY_CODE.getValue())
                        );
                    }

                    return kospiListingBasicInfo.update(
                            toInt(part2Map.get(Stock.PAR_VALUE.getValue())),
                            toDateTime(part2Map.get(Stock.LISTING_DATE.getValue())),
                            toInt(part2Map.get(Stock.LISTED_SHARES.getValue())),
                            toLong(part2Map.get(Stock.CAPITAL.getValue())),
                            toInt(part2Map.get(Stock.FISCAL_MONTH.getValue())),
                            toInt(part2Map.get(Stock.IPO_PRICE.getValue())),
                            part2Map.get(Stock.GROUP_COMPANY_CODE.getValue())
                    );
                })
                .toList();
        kospiListingBasicInfoRepository.saveAll(updated);
    }

    private void upsertRiskFlag(Map<String, KospiInfo> updatedInfos, List<KospiMaster> upserted, Set<Long> upsertedIds) {
        Map<Long, KospiRiskFlag> riskFlagMap = kospiRiskFlagRepository.findAllByKospiMasterIdIn(upsertedIds)
                .stream()
                .collect(Collectors.toMap(KospiRiskFlag::getKospiMasterId, Function.identity()));

        List<KospiRiskFlag> updated = upserted
                .stream()
                .map(kospiMaster -> {
                    KospiInfo kospiInfo = updatedInfos.get(kospiMaster.getShortCode());
                    Map<String, String> part2Map = kospiInfo.part2Map();

                    KospiRiskFlag kospiRiskFlag = riskFlagMap.get(kospiMaster.getId());

                    if (kospiRiskFlag == null) {
                        return new KospiRiskFlag(
                                kospiMaster.getId(),
                                part2Map.get(Stock.LOW_LIQUIDITY.getValue()),
                                toInt(part2Map.get(Stock.SHORT_TERM_OVERHEAT.getValue())),
                                toInt(part2Map.get(Stock.MARKET_WARNING.getValue())),
                                part2Map.get(Stock.WARNING_NOTICE.getValue()),
                                part2Map.get(Stock.SHORT_SELL_OVERHEAT.getValue()),
                                part2Map.get(Stock.ABNORMAL_RISE.getValue())
                        );
                    }

                    return kospiRiskFlag.update(
                            part2Map.get(Stock.LOW_LIQUIDITY.getValue()),
                            toInt(part2Map.get(Stock.SHORT_TERM_OVERHEAT.getValue())),
                            toInt(part2Map.get(Stock.MARKET_WARNING.getValue())),
                            part2Map.get(Stock.WARNING_NOTICE.getValue()),
                            part2Map.get(Stock.SHORT_SELL_OVERHEAT.getValue()),
                            part2Map.get(Stock.ABNORMAL_RISE.getValue())
                    );
                })
                .toList();
        kospiRiskFlagRepository.saveAll(updated);
    }

    private void upsertSectorInfo(Map<String, KospiInfo> updatedInfos, List<KospiMaster> upserted, Set<Long> upsertedIds) {
        Map<Long, KospiSectorInfo> sectorMap = kospiSectorInfoRepository.findAllByKospiMasterIdIn(upsertedIds)
                .stream()
                .collect(Collectors.toMap(KospiSectorInfo::getKospiMasterId, Function.identity()));

        List<KospiSectorInfo> updated = upserted
                .stream()
                .map(kospiMaster -> {
                    KospiInfo kospiInfo = updatedInfos.get(kospiMaster.getShortCode());
                    Map<String, String> part2Map = kospiInfo.part2Map();

                    KospiSectorInfo kospiSectorInfo = sectorMap.get(kospiMaster.getId());

                    if (kospiSectorInfo == null) {
                        return new KospiSectorInfo(
                                kospiMaster.getId(),
                                part2Map.get(Stock.KOSPI200_SECTOR.getValue()),
                                part2Map.get(Stock.KRX_CAR.getValue()),
                                part2Map.get(Stock.KRX_SEMICONDUCTOR.getValue()),
                                part2Map.get(Stock.KRX_BIO.getValue()),
                                part2Map.get(Stock.KRX_BANK.getValue()),
                                part2Map.get(Stock.KRX_ENERGY_CHEM.getValue()),
                                part2Map.get(Stock.KRX_STEEL.getValue()),
                                part2Map.get(Stock.KRX_MEDIA_COMM.getValue()),
                                part2Map.get(Stock.KRX_CONSTRUCTION.getValue()),
                                part2Map.get(Stock.KRX_SECURITIES.getValue()),
                                part2Map.get(Stock.KRX_SHIP.getValue()),
                                part2Map.get(Stock.KRX_SECTOR_INSURANCE.getValue()),
                                part2Map.get(Stock.KRX_SECTOR_TRANSPORT.getValue())
                        );
                    }

                    return kospiSectorInfo.update(
                            part2Map.get(Stock.KOSPI200_SECTOR.getValue()),
                            part2Map.get(Stock.KRX_CAR.getValue()),
                            part2Map.get(Stock.KRX_SEMICONDUCTOR.getValue()),
                            part2Map.get(Stock.KRX_BIO.getValue()),
                            part2Map.get(Stock.KRX_BANK.getValue()),
                            part2Map.get(Stock.KRX_ENERGY_CHEM.getValue()),
                            part2Map.get(Stock.KRX_STEEL.getValue()),
                            part2Map.get(Stock.KRX_MEDIA_COMM.getValue()),
                            part2Map.get(Stock.KRX_CONSTRUCTION.getValue()),
                            part2Map.get(Stock.KRX_SECURITIES.getValue()),
                            part2Map.get(Stock.KRX_SHIP.getValue()),
                            part2Map.get(Stock.KRX_SECTOR_INSURANCE.getValue()),
                            part2Map.get(Stock.KRX_SECTOR_TRANSPORT.getValue())
                    );
                })
                .toList();
        kospiSectorInfoRepository.saveAll(updated);
    }

    private void upsertTradingInfo(Map<String, KospiInfo> updatedInfos, List<KospiMaster> upserted, Set<Long> upsertedIds) {
        Map<Long, KospiTradingInfo> tradingInfoMap = kospiTradingInfoRepository.findAllByKospiMasterIdIn(upsertedIds)
                .stream()
                .collect(Collectors.toMap(KospiTradingInfo::getKospiMasterId, Function.identity()));

        List<KospiTradingInfo> updated = upserted
                .stream()
                .map(kospiMaster -> {
                    KospiInfo kospiInfo = updatedInfos.get(kospiMaster.getShortCode());
                    Map<String, String> part2Map = kospiInfo.part2Map();

                    KospiTradingInfo kospiTradingInfo = tradingInfoMap.get(kospiMaster.getId());

                    if (kospiTradingInfo == null) {
                        return new KospiTradingInfo(
                                kospiMaster.getId(),
                                toInt(part2Map.get(Stock.BASE_PRICE.getValue())),
                                toInt(part2Map.get(Stock.TRADE_UNIT.getValue())),
                                toInt(part2Map.get(Stock.AFTER_HOURS_UNIT.getValue())),
                                toInt(part2Map.get(Stock.MARGIN_RATE.getValue())),
                                part2Map.get(Stock.CREDIT_AVAILABLE.getValue()),
                                toInt(part2Map.get(Stock.CREDIT_DAYS.getValue())),
                                toInt(part2Map.get(Stock.PREVIOUS_DAY_VOLUME.getValue())),
                                part2Map.get(Stock.CREDIT_LIMIT_OVER.getValue()),
                                part2Map.get(Stock.COLLATERAL_LOAN_AVAILABLE.getValue()),
                                part2Map.get(Stock.STOCK_LENDING_AVAILABLE.getValue())
                        );
                    }

                    return kospiTradingInfo.update(
                            toInt(part2Map.get(Stock.BASE_PRICE.getValue())),
                            toInt(part2Map.get(Stock.TRADE_UNIT.getValue())),
                            toInt(part2Map.get(Stock.AFTER_HOURS_UNIT.getValue())),
                            toInt(part2Map.get(Stock.MARGIN_RATE.getValue())),
                            part2Map.get(Stock.CREDIT_AVAILABLE.getValue()),
                            toInt(part2Map.get(Stock.CREDIT_DAYS.getValue())),
                            toInt(part2Map.get(Stock.PREVIOUS_DAY_VOLUME.getValue())),
                            part2Map.get(Stock.CREDIT_LIMIT_OVER.getValue()),
                            part2Map.get(Stock.COLLATERAL_LOAN_AVAILABLE.getValue()),
                            part2Map.get(Stock.STOCK_LENDING_AVAILABLE.getValue())
                    );
                })
                .toList();
        kospiTradingInfoRepository.saveAll(updated);
    }

    private void upsertTradingStatusFlag(Map<String, KospiInfo> updatedInfos, List<KospiMaster> upserted, Set<Long> upsertedIds) {
        Map<Long, KospiTradingStatusFlag> kospiTradingStatusFlagMap = kospiTradingStatusFlagRepository.findAllByKospiMasterIdIn(upsertedIds)
                .stream()
                .collect(Collectors.toMap(KospiTradingStatusFlag::getKospiMasterId, Function.identity()));

        List<KospiTradingStatusFlag> updated = upserted
                .stream()
                .map(kospiMaster -> {
                    KospiInfo kospiInfo = updatedInfos.get(kospiMaster.getShortCode());
                    Map<String, String> part2Map = kospiInfo.part2Map();

                    KospiTradingStatusFlag kospiTradingStatusFlag = kospiTradingStatusFlagMap.get(kospiMaster.getId());

                    if (kospiTradingStatusFlag == null) {
                        return new KospiTradingStatusFlag(
                                kospiMaster.getId(),
                                part2Map.get(Stock.TRADING_HALT.getValue()),
                                part2Map.get(Stock.LIQUIDATION.getValue()),
                                part2Map.get(Stock.MANAGEMENT_ISSUE.getValue()),
                                part2Map.get(Stock.GOVERNANCE_INDEX.getValue()),
                                part2Map.get(Stock.POOR_DISCLOSURE.getValue()),
                                part2Map.get(Stock.BACKDOOR_LISTING.getValue())
                        );
                    }

                    return kospiTradingStatusFlag.update(
                            part2Map.get(Stock.TRADING_HALT.getValue()),
                            part2Map.get(Stock.LIQUIDATION.getValue()),
                            part2Map.get(Stock.MANAGEMENT_ISSUE.getValue()),
                            part2Map.get(Stock.GOVERNANCE_INDEX.getValue()),
                            part2Map.get(Stock.POOR_DISCLOSURE.getValue()),
                            part2Map.get(Stock.BACKDOOR_LISTING.getValue())
                    );
                })
                .toList();
        kospiTradingStatusFlagRepository.saveAll(updated);
    }


    private void upsertTypeInfo(Map<String, KospiInfo> updatedInfos, List<KospiMaster> upserted, Set<Long> upsertedIds) {
        Map<Long, KospiTypeInfo> typeInfoMap = kospiTypeInfoRepository.findAllByKospiMasterIdIn(upsertedIds)
                .stream()
                .collect(Collectors.toMap(KospiTypeInfo::getKospiMasterId, Function.identity()));

        List<KospiTypeInfo> updated = upserted
                .stream()
                .map(kospiMaster -> {
                    KospiInfo kospiInfo = updatedInfos.get(kospiMaster.getShortCode());
                    Map<String, String> part2Map = kospiInfo.part2Map();

                    KospiTypeInfo kospiTypeInfo = typeInfoMap.get(kospiMaster.getId());

                    if (kospiTypeInfo == null) {
                        return new KospiTypeInfo(
                                kospiMaster.getId(),
                                part2Map.get(Stock.GROUP_CODE.getValue()),
                                toInt(part2Map.get(Stock.MARKET_CAP_SIZE.getValue())),
                                part2Map.get(Stock.MANUFACTURING.getValue()),
                                toInt(part2Map.get(Stock.ETP.getValue())),
                                part2Map.get(Stock.SPAC.getValue()),
                                part2Map.get(Stock.ELW_ISSUED.getValue())
                        );
                    }

                    return kospiTypeInfo.update(
                            part2Map.get(Stock.GROUP_CODE.getValue()),
                            toInt(part2Map.get(Stock.MARKET_CAP_SIZE.getValue())),
                            part2Map.get(Stock.MANUFACTURING.getValue()),
                            toInt(part2Map.get(Stock.ETP.getValue())),
                            part2Map.get(Stock.SPAC.getValue()),
                            part2Map.get(Stock.ELW_ISSUED.getValue())
                    );
                })
                .toList();
        kospiTypeInfoRepository.saveAll(updated);
    }

    // 상장폐지된 주식 DB 에서 제거
    private void removeDeListed(Map<String, KospiMaster> oldInfos, Map<String, KospiInfo> updatedInfos) {
        for (KospiMaster old : oldInfos.values()) {
            if (updatedInfos.get(old.getShortCode()) == null) {
                old.updateStatus(true);
            }
        }
    }

    private Double toDouble(String value) {
        return Optional.ofNullable(value)
                .map(String::trim)
                .filter(v -> !v.isBlank())
                .map(Double::parseDouble)
                .orElse(null);
    }

    private Integer toInt(String value) {
        return Optional.ofNullable(value)
                .filter(v -> !v.trim().isBlank())
                .map(Integer::parseInt)
                .orElse(null);
    }

    private Long toLong(String value) {
        return Optional.ofNullable(value)
                .filter(v -> !v.trim().isBlank())
                .map(Long::parseLong)
                .orElse(null);
    }

    private LocalDateTime toDateTime(String value) {
        return Optional.ofNullable(value)
                .map(String::trim)
                .filter(v -> !v.trim().isBlank())
                .map(v -> LocalDate.parse(v, DATE_FORMAT).atStartOfDay())
                .orElse(null);
    }

    private record KospiInfo(
            String shortCode,
            String standardCode,
            String nameKr,
            Map<String, String> part2Map
    ) {
    }
}
