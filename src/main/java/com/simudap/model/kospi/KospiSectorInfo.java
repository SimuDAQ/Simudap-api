package com.simudap.model.kospi;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(schema = "simudaq")
@NoArgsConstructor
public class KospiSectorInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 외래키
    @Column(nullable = false)
    private Long kospiMasterId;

    // KOSPI200 섹터업종
    // (0:미분류, 1:건설기계, 2:조선운송, 3:철강소재,
    //  4:에너지화학, 5:정보통신, 6:금융, 7:필수소비재, 8:자유소비재)
    @Column(name = "kospi200_apnt_cls_code", length = 1)
    private String kospi200SectorCode;

    // KRX 자동차(Y/N)
    @Column(name = "krx_car_yn", length = 1)
    private String krxCarYn;

    // KRX 반도체(Y/N)
    @Column(name = "krx_smcn_yn", length = 1)
    private String krxSemiconductorYn;

    // KRX 바이오(Y/N)
    @Column(name = "krx_bio_yn", length = 1)
    private String krxBioYn;

    // KRX 은행(Y/N)
    @Column(name = "krx_bank_yn", length = 1)
    private String krxBankYn;

    // KRX 에너지화학(Y/N)
    @Column(name = "krx_enrg_chms_yn", length = 1)
    private String krxEnergyChemYn;

    // KRX 철강(Y/N)
    @Column(name = "krx_stel_yn", length = 1)
    private String krxSteelYn;

    // KRX 미디어·통신(Y/N)
    @Column(name = "krx_medi_cmnc_yn", length = 1)
    private String krxMediaCommYn;

    // KRX 건설(Y/N)
    @Column(name = "krx_cnst_yn", length = 1)
    private String krxConstructionYn;

    // KRX 증권(Y/N)
    @Column(name = "krx_scrt_yn", length = 1)
    private String krxSecuritiesYn;

    // KRX 선박(Y/N)
    @Column(name = "krx_ship_yn", length = 1)
    private String krxShipYn;

    // KRX 보험(Y/N)
    @Column(name = "krx_insu_yn", length = 1)
    private String krxInsuranceYn;

    // KRX 운송(Y/N)
    @Column(name = "krx_trnp_yn", length = 1)
    private String krxTransportYn;

    public KospiSectorInfo(Long kospiMasterId,
                           String kospi200SectorCode,
                           String krxCarYn,
                           String krxSemiconductorYn,
                           String krxBioYn,
                           String krxBankYn,
                           String krxEnergyChemYn,
                           String krxSteelYn,
                           String krxMediaCommYn,
                           String krxConstructionYn,
                           String krxSecuritiesYn,
                           String krxShipYn,
                           String krxInsuranceYn,
                           String krxTransportYn) {
        this.kospiMasterId = kospiMasterId;
        this.kospi200SectorCode = kospi200SectorCode;
        this.krxCarYn = krxCarYn;
        this.krxSemiconductorYn = krxSemiconductorYn;
        this.krxBioYn = krxBioYn;
        this.krxBankYn = krxBankYn;
        this.krxEnergyChemYn = krxEnergyChemYn;
        this.krxSteelYn = krxSteelYn;
        this.krxMediaCommYn = krxMediaCommYn;
        this.krxConstructionYn = krxConstructionYn;
        this.krxSecuritiesYn = krxSecuritiesYn;
        this.krxShipYn = krxShipYn;
        this.krxInsuranceYn = krxInsuranceYn;
        this.krxTransportYn = krxTransportYn;
    }

    public KospiSectorInfo update(String kospi200SectorCode,
                                  String krxCarYn,
                                  String krxSemiconductorYn,
                                  String krxBioYn,
                                  String krxBankYn,
                                  String krxEnergyChemYn,
                                  String krxSteelYn,
                                  String krxMediaCommYn,
                                  String krxConstructionYn,
                                  String krxSecuritiesYn,
                                  String krxShipYn,
                                  String krxInsuranceYn,
                                  String krxTransportYn) {
        this.kospi200SectorCode = kospi200SectorCode;
        this.krxCarYn = krxCarYn;
        this.krxSemiconductorYn = krxSemiconductorYn;
        this.krxBioYn = krxBioYn;
        this.krxBankYn = krxBankYn;
        this.krxEnergyChemYn = krxEnergyChemYn;
        this.krxSteelYn = krxSteelYn;
        this.krxMediaCommYn = krxMediaCommYn;
        this.krxConstructionYn = krxConstructionYn;
        this.krxSecuritiesYn = krxSecuritiesYn;
        this.krxShipYn = krxShipYn;
        this.krxInsuranceYn = krxInsuranceYn;
        this.krxTransportYn = krxTransportYn;

        return this;
    }
}
