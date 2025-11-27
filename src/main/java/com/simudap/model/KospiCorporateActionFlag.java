package com.simudap.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(schema = "simudaq")
@NoArgsConstructor
public class KospiCorporateActionFlag {    // 락/액면/증자/우선주 등 기업 액션 관련 플래그

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    // KospiMaster.id 외래키
    @Column(nullable = false)
    private long kospiMasterId;

    // 락구분 코드 (flng_cls_code)
    // 00:해당없음, 01:권리락, 02:배당락, 03:분배락,
    // 04:권배락, 05:중간배당락, 06:권리중간배당락, 99:기타
    @Column(name = "flng_cls_code", length = 2)
    private Integer lockTypeCode;

    // 액면가 변경 구분 코드 (fcam_mod_cls_code)
    // 00:해당없음, 01:액면분할, 02:액면병합, 99:기타
    @Column(name = "fcam_mod_cls_code", length = 2)
    private Integer parValueChangeCode;

    // 증자 구분 코드 (icic_cls_code)
    // 00:해당없음, 01:유상증자, 02:무상증자, 03:유무상증자, 99:기타
    @Column(name = "icic_cls_code", length = 2)
    private Integer capitalIncreaseCode;

    // 우선주 구분 코드 (prst_cls_code)
    // 0:보통주, 1:구형우선주, 2:신형우선주
    @Column(name = "prst_cls_code", length = 1)
    private Integer preferredStockCode;

    public KospiCorporateActionFlag(long kospiMasterId,
                                    Integer lockTypeCode,
                                    Integer parValueChangeCode,
                                    Integer capitalIncreaseCode,
                                    Integer preferredStockCode) {
        this.kospiMasterId = kospiMasterId;
        this.lockTypeCode = lockTypeCode;
        this.parValueChangeCode = parValueChangeCode;
        this.capitalIncreaseCode = capitalIncreaseCode;
        this.preferredStockCode = preferredStockCode;
    }

    public KospiCorporateActionFlag update(Integer lockTypeCode,
                                           Integer parValueChangeCode,
                                           Integer capitalIncreaseCode,
                                           Integer preferredStockCode) {
        this.lockTypeCode = lockTypeCode;
        this.parValueChangeCode = parValueChangeCode;
        this.capitalIncreaseCode = capitalIncreaseCode;
        this.preferredStockCode = preferredStockCode;

        return this;
    }
}
