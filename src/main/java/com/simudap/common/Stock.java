package com.simudap.common;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Stock {
    GROUP_CODE("그룹코드", 2, DataType.STRING),
    MARKET_CAP_SIZE("시가총액규모", 1, DataType.INTEGER),
    INDEX_INDUSTRY_LARGE("지수업종대분류",4, DataType.INTEGER),
    INDEX_INDUSTRY_MID("지수업종중분류", 4, DataType.INTEGER),
    INDEX_INDUSTRY_SMALL("지수업종소분류",4, DataType.INTEGER),
    MANUFACTURING("제조업",1, DataType.STRING),
    LOW_LIQUIDITY("저유동성",1, DataType.STRING),
    GOVERNANCE_INDEX("지배구조지수종목",1, DataType.STRING),
    KOSPI200_SECTOR("KOSPI200섹터업종",1, DataType.STRING),
    KOSPI100("KOSPI100",1, DataType.STRING),
    KOSPI50("KOSPI50",1, DataType.STRING),
    KRX("KRX",1, DataType.STRING),
    ETP("ETP",1, DataType.INTEGER),
    ELW_ISSUED("ELW발행",1, DataType.STRING),
    KRX100("KRX100",1, DataType.STRING),
    KRX_CAR("KRX자동차",1, DataType.STRING),
    KRX_SEMICONDUCTOR("KRX반도체",1, DataType.STRING),
    KRX_BIO("KRX바이오",1, DataType.STRING),
    KRX_BANK("KRX은행",1, DataType.STRING),
    SPAC("SPAC",1, DataType.STRING), // 24
    KRX_ENERGY_CHEM("KRX에너지화학",1, DataType.STRING),
    KRX_STEEL("KRX철강",1, DataType.STRING),
    SHORT_TERM_OVERHEAT("단기과열",1, DataType.INTEGER),
    KRX_MEDIA_COMM("KRX미디어통신",1, DataType.STRING),
    KRX_CONSTRUCTION("KRX건설",1, DataType.STRING), // 29
    NON1("삭제종목",1,DataType.STRING),
    KRX_SECURITIES("KRX증권",1, DataType.STRING),
    KRX_SHIP("KRX선박",1, DataType.STRING),
    KRX_SECTOR_INSURANCE("KRX섹터_보험",1, DataType.STRING),
    KRX_SECTOR_TRANSPORT("KRX섹터_운송",1, DataType.STRING),
    SRI("SRI",1, DataType.STRING),
    BASE_PRICE("기준가",9, DataType.INTEGER), // 36
    TRADE_UNIT("매매수량단위",5, DataType.INTEGER),
    AFTER_HOURS_UNIT("시간외수량단위",5, DataType.INTEGER),
    TRADING_HALT("거래정지",1, DataType.STRING),
    LIQUIDATION("정리매매",1, DataType.STRING),
    MANAGEMENT_ISSUE("관리종목",1, DataType.STRING), // 41
    MARKET_WARNING("시장경고",2, DataType.INTEGER),
    WARNING_NOTICE("경고예고",1, DataType.STRING),
    POOR_DISCLOSURE("불성실공시",1, DataType.STRING),
    BACKDOOR_LISTING("우회상장",1, DataType.STRING),
    LOCK_TYPE("락구분",2, DataType.INTEGER),
    PAR_VALUE_CHANGE("액면변경",2, DataType.INTEGER),
    CAPITAL_INCREASE_TYPE("증자구분",2, DataType.INTEGER),
    MARGIN_RATE("증거금비율",3, DataType.INTEGER),
    CREDIT_AVAILABLE("신용가능",1, DataType.STRING), //50
    CREDIT_DAYS("신용기간",3, DataType.INTEGER),
    PREVIOUS_DAY_VOLUME("전일거래량",12, DataType.INTEGER),
    PAR_VALUE("액면가",12, DataType.INTEGER),
    LISTING_DATE("상장일자",8, DataType.DATE_TIME), //54
    LISTED_SHARES("상장주수",15, DataType.INTEGER),
    CAPITAL("자본금",21, DataType.LONG),  //56
    FISCAL_MONTH("결산월",2, DataType.INTEGER),
    IPO_PRICE("공모가",7, DataType.INTEGER),
    PREFERRED_STOCK("우선주",1, DataType.INTEGER),
    SHORT_SELL_OVERHEAT("공매도과열",1, DataType.STRING), //60
    ABNORMAL_RISE("이상급등",1, DataType.STRING),
    KRX300("KRX300",1, DataType.STRING),
    KOSPI("KOSPI",1, DataType.STRING), //63
    SALES("매출액",9, DataType.INTEGER),
    OPERATING_PROFIT("영업이익",9, DataType.INTEGER),
    ORDINARY_PROFIT("경상이익",9, DataType.INTEGER),
    NET_INCOME("당기순이익",5, DataType.INTEGER),
    ROE("ROE",9, DataType.DOUBLE), //68
    REFERENCE_YEAR_MONTH("기준년월",8, DataType.DATE_TIME),
    MARKET_CAP("시가총액",9, DataType.INTEGER),
    GROUP_COMPANY_CODE("그룹사코드",3, DataType.STRING),
    CREDIT_LIMIT_OVER("회사신용한도초과",1, DataType.STRING),
    COLLATERAL_LOAN_AVAILABLE("담보대출가능",1, DataType.STRING),
    STOCK_LENDING_AVAILABLE("대주가능",1, DataType.STRING);

    private final String value;
    private final int fieldSpace;
    private final DataType dataType;

    public enum DataType {
        STRING,
        INTEGER,
        LONG,
        DATE_TIME,
        DOUBLE
    }
}
