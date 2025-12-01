package com.simudap.model.kospi;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(schema = "simudaq")
@NoArgsConstructor
public class KospiTypeInfo {    // 이 종목이 어떤 종류/상품/규모인지

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    // 외래키
    @Column(nullable = false)
    private long kospiMasterId;

    // 증권그룹구분코드
    // ST:주권, MF:증권투자회사, RT:부동산투자회사, SC:선박투자회사
    // IF:사회간접자본투융자회사, DR:주식예탁증서, EW:ELW, EF:ETF
    // SW:신주인수권증권, SR:신주인수권증서, BC:수익증권, FE:해외ETF, FS:외국주권
    @Column(name = "scrt_grp_cls_code", length = 2)
    private String securityGroupCode;

    // 시가총액 규모 구분 코드 (0:제외 1:대 2:중 3:소)
    @Column(name = "avls_scal_cls_code", length = 1)
    private Integer marketCapSizeCode;

    // 제조업 구분 코드 (Y/N)
    @Column(name = "mnin_cls_code_yn", length = 1)
    private String manufacturingYn;

    // ETP 상품구분코드
    // 0:해당없음 1:투자회사형 2:수익증권형 3:ETN 4:손실제한ETN
    @Column(name = "etp_prod_cls_code", length = 1)
    private Integer etpProductClassCode;

    // 기업인수목적회사(SPAC) 여부 (Y/N)
    @Column(name = "etpr_undt_objt_co_yn", length = 1)
    private String spacYn;

    // ELW 발행여부 (Y/N) → 파생상품/유형 측면에서 함께 배치
    @Column(name = "elw_pblc_yn", length = 1)
    private String elwPublicYn;

    public KospiTypeInfo(long kospiMasterId,
                         String securityGroupCode,
                         Integer marketCapSizeCode,
                         String manufacturingYn,
                         Integer etpProductClassCode,
                         String spacYn,
                         String elwPublicYn) {
        this.kospiMasterId = kospiMasterId;
        this.securityGroupCode = securityGroupCode;
        this.marketCapSizeCode = marketCapSizeCode;
        this.manufacturingYn = manufacturingYn;
        this.etpProductClassCode = etpProductClassCode;
        this.spacYn = spacYn;
        this.elwPublicYn = elwPublicYn;
    }

    public KospiTypeInfo update(String securityGroupCode, Integer marketCapSizeCode, String manufacturingYn, Integer etpProductClassCode, String spacYn, String elwPublicYn) {
        this.securityGroupCode = securityGroupCode;
        this.marketCapSizeCode = marketCapSizeCode;
        this.manufacturingYn = manufacturingYn;
        this. etpProductClassCode = etpProductClassCode;
        this.spacYn = spacYn;
        this.elwPublicYn = elwPublicYn;

        return this;
    }
}
