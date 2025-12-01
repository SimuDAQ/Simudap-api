package com.simudap.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(schema = "simudaq")
@NoArgsConstructor
public class KospiRiskFlag {    // 유동성/단기과열/시장경고/이상급등 등 리스크 관련 플래그

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    // 외래키
    @Column(nullable = false)
    private long kospiMasterId;

    // 저유동성종목 여부 (Y/N)
    @Column(name = "low_current_yn", length = 1)
    private String lowLiquidityYn;

    // 단기과열종목 구분 코드
    // 0:해당없음, 1:지정예고, 2:지정, 3:지정연장(해제연기)
    @Column(name = "short_over_cls_code", length = 1)
    private Integer shortOverheatCode;

    // 시장경고 구분 코드
    // 00:해당없음, 01:투자주의, 02:투자경고, 03:투자위험
    @Column(name = "mrkt_alrm_cls_code", length = 2)
    private Integer marketAlarmCode;

    // 시장경고위험 예고 여부 (Y/N)
    @Column(name = "mrkt_alrm_risk_adnt_yn", length = 1)
    private String marketAlarmRiskAlertYn;

    // 공매도과열종목 여부 (Y/N)
    @Column(name = "ssts_hot_yn", length = 1)
    private String shortSaleOverheatYn;

    // 이상급등종목 여부 (Y/N)
    @Column(name = "stange_runup_yn", length = 1)
    private String abnormalRunupYn;

    public KospiRiskFlag(long kospiMasterId,
                         String lowLiquidityYn,
                         Integer shortOverheatCode,
                         Integer marketAlarmCode,
                         String marketAlarmRiskAlertYn,
                         String shortSaleOverheatYn,
                         String abnormalRunupYn) {
        this.kospiMasterId = kospiMasterId;
        this.lowLiquidityYn = lowLiquidityYn;
        this.shortOverheatCode = shortOverheatCode;
        this.marketAlarmCode = marketAlarmCode;
        this.marketAlarmRiskAlertYn = marketAlarmRiskAlertYn;
        this.shortSaleOverheatYn = shortSaleOverheatYn;
        this.abnormalRunupYn = abnormalRunupYn;
    }

    public KospiRiskFlag update(String lowLiquidityYn,
                                Integer shortOverheatCode,
                                Integer marketAlarmCode,
                                String marketAlarmRiskAlertYn,
                                String shortSaleOverheatYn,
                                String abnormalRunupYn) {
        this.lowLiquidityYn = lowLiquidityYn;
        this.shortOverheatCode = shortOverheatCode;
        this.marketAlarmCode = marketAlarmCode;
        this.marketAlarmRiskAlertYn = marketAlarmRiskAlertYn;
        this.shortSaleOverheatYn = shortSaleOverheatYn;
        this.abnormalRunupYn = abnormalRunupYn;

        return this;
    }
}
