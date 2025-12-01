package com.simudap.model.kospi;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(schema = "simudaq")
public class KospiFinancialInfo {    // 재무, 실적, 시총 정보

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    // KospiMaster.id 외래키
    @Column(nullable = false)
    private long kospiMasterId;

    // 매출액
    @Column(name = "sale_account", length = 9)
    private Integer sales;

    // 영업이익
    @Column(name = "bsop_prfi", length = 9)
    private Integer operatingProfit;

    // 경상이익
    @Column(name = "op_prfi", length = 9)
    private Integer ordinaryProfit;

    // 당기순이익
    @Column(name = "thtr_ntin", length = 5)
    private Integer netIncome;

    // ROE
    @Column(name = "roe", length = 9)
    private Double roe;

    // 기준년월 (YYYYMM)
    @Column(name = "base_date", length = 8)
    private LocalDateTime baseDate;

    // 전일 기준 시가총액 (억)
    @Column(name = "prdy_avls_scal", length = 9)
    private Integer previousDayMarketCap;

    public KospiFinancialInfo(long kospiMasterId,
                              Integer sales,
                              Integer operatingProfit,
                              Integer ordinaryProfit,
                              Integer netIncome,
                              Double roe,
                              LocalDateTime baseDate,
                              Integer previousDayMarketCap) {
        this.kospiMasterId = kospiMasterId;
        this.sales = sales;
        this.operatingProfit = operatingProfit;
        this.ordinaryProfit = ordinaryProfit;
        this.netIncome = netIncome;
        this.roe = roe;
        this.baseDate = baseDate;
        this.previousDayMarketCap = previousDayMarketCap;
    }

    public KospiFinancialInfo update(Integer sales,
                                     Integer operatingProfit,
                                     Integer ordinaryProfit,
                                     Integer netIncome,
                                     Double roe,
                                     LocalDateTime baseDate,
                                     Integer previousDayMarketCap) {
        this.sales = sales;
        this.operatingProfit = operatingProfit;
        this.ordinaryProfit = ordinaryProfit;
        this.netIncome = netIncome;
        this.roe = roe;
        this.baseDate = baseDate;
        this.previousDayMarketCap = previousDayMarketCap;

        return this;
    }
}
