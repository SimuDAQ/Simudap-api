package com.simudap.model.kospi;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(schema = "simudaq")
@NoArgsConstructor
public class KospiTradingStatusFlag {   // 거래 상태(정지/정리매매/관리종목), 지배구조/공시/우회상장 관련 플래그

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    // 외래키
    @Column(nullable = false)
    private long kospiMasterId;

    // 거래정지 여부 (Y/N)
    @Column(name = "trht_yn", length = 1)
    private String tradingHaltYn;

    // 정리매매 여부 (Y/N)
    @Column(name = "sltr_yn", length = 1)
    private String liquidationYn;

    // 관리종목 여부 (Y/N)
    @Column(name = "mang_issu_yn", length = 1)
    private String managementIssueYn;

    // 지배구조지수 종목 여부 (Y/N)
    @Column(name = "sprn_strr_nmix_issu_yn", length = 1)
    private String governanceIndexYn;

    // 불성실공시 여부 (Y/N)
    @Column(name = "insn_pbnt_yn", length = 1)
    private String poorDisclosureYn;

    // 우회상장 여부 (Y/N)
    @Column(name = "byps_lstn_yn", length = 1)
    private String backdoorListingYn;

    public KospiTradingStatusFlag(long kospiMasterId,
                                  String tradingHaltYn,
                                  String liquidationYn,
                                  String managementIssueYn,
                                  String governanceIndexYn,
                                  String poorDisclosureYn,
                                  String backdoorListingYn) {
        this.kospiMasterId = kospiMasterId;
        this.tradingHaltYn = tradingHaltYn;
        this.liquidationYn = liquidationYn;
        this.managementIssueYn = managementIssueYn;
        this.governanceIndexYn = governanceIndexYn;
        this.poorDisclosureYn = poorDisclosureYn;
        this.backdoorListingYn = backdoorListingYn;
    }

    public KospiTradingStatusFlag update(String tradingHaltYn, String liquidationYn, String managementIssueYn, String governanceIndexYn, String poorDisclosureYn, String backdoorListingYn) {
        this.tradingHaltYn = tradingHaltYn;
        this.liquidationYn = liquidationYn;
        this.managementIssueYn = managementIssueYn;
        this.governanceIndexYn = governanceIndexYn;
        this.poorDisclosureYn = poorDisclosureYn;
        this.backdoorListingYn = backdoorListingYn;

        return this;
    }
}
