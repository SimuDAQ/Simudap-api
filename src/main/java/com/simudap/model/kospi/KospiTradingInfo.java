package com.simudap.model.kospi;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(schema = "simudaq")
public class KospiTradingInfo {     // 거래조건, 신용, 대주 정보

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    // 외래키
    @Column(nullable = false)
    private long kospiMasterId;

    // 주식 기준가
    @Column(name = "stck_sdpr", length = 9)
    private Integer basePrice;

    // 정규 시장 매매 수량 단위
    @Column(name = "frml_mrkt_deal_qty_unit", length = 5)
    private Integer regularMarketQtyUnit;

    // 시간외 시장 매매 수량 단위
    @Column(name = "ovtm_mrkt_deal_qty_unit", length = 5)
    private Integer afterHoursMarketQtyUnit;

    // 증거금 비율
    @Column(name = "marg_rate", length = 3)
    private Integer marginRate;

    // 신용주문 가능 여부
    @Column(name = "crdt_able", length = 1)
    private String creditAble;

    // 신용 기간
    @Column(name = "crdt_days", length = 3)
    private Integer creditDays;

    // 전일 거래량
    @Column(name = "prdy_vol", length = 12)
    private Integer previousDayVolume;

    // 회사신용한도초과 여부
    @Column(name = "co_crdt_limt_over_yn", length = 1)
    private String creditLimitOverYn;

    // 담보대출 가능 여부
    @Column(name = "secu_lend_able_yn", length = 1)
    private String collateralLoanAbleYn;

    // 대주 가능 여부
    @Column(name = "stln_able_yn", length = 1)
    private String stockLendAbleYn;

    public KospiTradingInfo(long kospiMasterId,
                            Integer basePrice,
                            Integer regularMarketQtyUnit,
                            Integer afterHoursMarketQtyUnit,
                            Integer marginRate,
                            String creditAble,
                            Integer creditDays,
                            Integer previousDayVolume,
                            String creditLimitOverYn,
                            String collateralLoanAbleYn,
                            String stockLendAbleYn) {
        this.kospiMasterId = kospiMasterId;
        this.basePrice = basePrice;
        this.regularMarketQtyUnit = regularMarketQtyUnit;
        this.afterHoursMarketQtyUnit = afterHoursMarketQtyUnit;
        this.marginRate = marginRate;
        this.creditAble = creditAble;
        this.creditDays = creditDays;
        this.previousDayVolume = previousDayVolume;
        this.creditLimitOverYn = creditLimitOverYn;
        this.collateralLoanAbleYn = collateralLoanAbleYn;
        this.stockLendAbleYn = stockLendAbleYn;
    }

    public KospiTradingInfo update(Integer basePrice,
                                   Integer regularMarketQtyUnit,
                                   Integer afterHoursMarketQtyUnit,
                                   Integer marginRate,
                                   String creditAble,
                                   Integer creditDays,
                                   Integer previousDayVolume,
                                   String creditLimitOverYn,
                                   String collateralLoanAbleYn,
                                   String stockLendAbleYn) {
        this.basePrice = basePrice;
        this.regularMarketQtyUnit = regularMarketQtyUnit;
        this.afterHoursMarketQtyUnit = afterHoursMarketQtyUnit;
        this.marginRate = marginRate;
        this.creditAble = creditAble;
        this.creditDays = creditDays;
        this.previousDayVolume = previousDayVolume;
        this.creditLimitOverYn = creditLimitOverYn;
        this.collateralLoanAbleYn = collateralLoanAbleYn;
        this.stockLendAbleYn = stockLendAbleYn;

        return this;
    }
}
