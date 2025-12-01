package com.simudap.enums.stock_info;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public enum KospiDetailInfo implements StockDetailInfo {
    TYPE(-1, "Enum 호출용", -1, DataType.STRING),
    GROUP_CODE(0, "그룹코드", 2, DataType.STRING),
    MARKET_CAP_SIZE(1, "시가총액규모", 1, DataType.INTEGER),
    INDEX_INDUSTRY_LARGE(2, "지수업종대분류", 4, DataType.INTEGER),
    INDEX_INDUSTRY_MID(3, "지수업종중분류", 4, DataType.INTEGER),
    INDEX_INDUSTRY_SMALL(4, "지수업종소분류", 4, DataType.INTEGER),
    MANUFACTURING(5, "제조업", 1, DataType.STRING),
    LOW_LIQUIDITY(6, "저유동성", 1, DataType.STRING),
    GOVERNANCE_INDEX(7, "지배구조지수종목", 1, DataType.STRING),
    KOSPI200_SECTOR(8, "KOSPI200섹터업종", 1, DataType.STRING),
    KOSPI100(9, "KOSPI100", 1, DataType.STRING),
    KOSPI50(10, "KOSPI50", 1, DataType.STRING),
    KRX(11, "KRX", 1, DataType.STRING),
    ETP(12, "ETP", 1, DataType.INTEGER),
    ELW_ISSUED(13, "ELW발행", 1, DataType.STRING),
    KRX100(14, "KRX100", 1, DataType.STRING),
    KRX_CAR(15, "KRX자동차", 1, DataType.STRING),
    KRX_SEMICONDUCTOR(16, "KRX반도체", 1, DataType.STRING),
    KRX_BIO(17, "KRX바이오", 1, DataType.STRING),
    KRX_BANK(18, "KRX은행", 1, DataType.STRING),
    SPAC(19, "SPAC", 1, DataType.STRING),
    KRX_ENERGY_CHEM(20, "KRX에너지화학", 1, DataType.STRING),
    KRX_STEEL(21, "KRX철강", 1, DataType.STRING),
    SHORT_TERM_OVERHEAT(22, "단기과열", 1, DataType.INTEGER),
    KRX_MEDIA_COMM(23, "KRX미디어통신", 1, DataType.STRING),
    KRX_CONSTRUCTION(24, "KRX건설", 1, DataType.STRING),
    NON1(25, "삭제종목", 1, DataType.STRING),
    KRX_SECURITIES(26, "KRX증권", 1, DataType.STRING),
    KRX_SHIP(27, "KRX선박", 1, DataType.STRING),
    KRX_SECTOR_INSURANCE(28, "KRX섹터_보험", 1, DataType.STRING),
    KRX_SECTOR_TRANSPORT(29, "KRX섹터_운송", 1, DataType.STRING),
    SRI(30, "SRI", 1, DataType.STRING),
    BASE_PRICE(31, "기준가", 9, DataType.INTEGER),
    TRADE_UNIT(32, "매매수량단위", 5, DataType.INTEGER),
    AFTER_HOURS_UNIT(33, "시간외수량단위", 5, DataType.INTEGER),
    TRADING_HALT(34, "거래정지", 1, DataType.STRING),
    LIQUIDATION(35, "정리매매", 1, DataType.STRING),
    MANAGEMENT_ISSUE(36, "관리종목", 1, DataType.STRING),
    MARKET_WARNING(37, "시장경고", 2, DataType.INTEGER),
    WARNING_NOTICE(38, "경고예고", 1, DataType.STRING),
    POOR_DISCLOSURE(39, "불성실공시", 1, DataType.STRING),
    BACKDOOR_LISTING(40, "우회상장", 1, DataType.STRING),
    LOCK_TYPE(41, "락구분", 2, DataType.INTEGER),
    PAR_VALUE_CHANGE(42, "액면변경", 2, DataType.INTEGER),
    CAPITAL_INCREASE_TYPE(43, "증자구분", 2, DataType.INTEGER),
    MARGIN_RATE(44, "증거금비율", 3, DataType.INTEGER),
    CREDIT_AVAILABLE(45, "신용가능", 1, DataType.STRING),
    CREDIT_DAYS(46, "신용기간", 3, DataType.INTEGER),
    PREVIOUS_DAY_VOLUME(47, "전일거래량", 12, DataType.INTEGER),
    PAR_VALUE(48, "액면가", 12, DataType.INTEGER),
    LISTING_DATE(49, "상장일자", 8, DataType.DATE_TIME),
    LISTED_SHARES(50, "상장주수", 15, DataType.INTEGER),
    CAPITAL(51, "자본금", 21, DataType.LONG),
    FISCAL_MONTH(52, "결산월", 2, DataType.INTEGER),
    IPO_PRICE(53, "공모가", 7, DataType.INTEGER),
    PREFERRED_STOCK(54, "우선주", 1, DataType.INTEGER),
    SHORT_SELL_OVERHEAT(55, "공매도과열", 1, DataType.STRING),
    ABNORMAL_RISE(56, "이상급등", 1, DataType.STRING),
    KRX300(57, "KRX300", 1, DataType.STRING),
    KOSPI(58, "KOSPI", 1, DataType.STRING),
    SALES(59, "매출액", 9, DataType.INTEGER),
    OPERATING_PROFIT(60, "영업이익", 9, DataType.INTEGER),
    ORDINARY_PROFIT(61, "경상이익", 9, DataType.INTEGER),
    NET_INCOME(62, "당기순이익", 5, DataType.INTEGER),
    ROE(63, "ROE", 9, DataType.DOUBLE),
    REFERENCE_YEAR_MONTH(64, "기준년월", 8, DataType.DATE_TIME),
    MARKET_CAP(65, "시가총액", 9, DataType.INTEGER),
    GROUP_COMPANY_CODE(66, "그룹사코드", 3, DataType.STRING),
    CREDIT_LIMIT_OVER(67, "회사신용한도초과", 1, DataType.STRING),
    COLLATERAL_LOAN_AVAILABLE(68, "담보대출가능", 1, DataType.STRING),
    STOCK_LENDING_AVAILABLE(69, "대주가능", 1, DataType.STRING);

    private final int sequenceNum;
    private final String value;
    private final int fieldSpace;
    private final DataType dataType;

    @Override
    public Map<Integer, StockDetailInfo> convertToMap() {
        return Arrays.stream(KospiDetailInfo.values())
                .filter(e -> !e.name().equalsIgnoreCase(KospiDetailInfo.TYPE.name()))
                .collect(Collectors.toMap(v -> v.sequenceNum, Function.identity()));
    }

    public enum DataType {
        STRING,
        INTEGER,
        LONG,
        DATE_TIME,
        DOUBLE
    }
}
