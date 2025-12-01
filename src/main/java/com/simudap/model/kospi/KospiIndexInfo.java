package com.simudap.model.kospi;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(schema = "simudaq")
@NoArgsConstructor
public class KospiIndexInfo {    // 이 종목이 어떤 지수에 편입되어 있는지

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    // 외래키
    @Column(nullable = false)
    private long kospiMasterId;

    // KOSPI100 여부 (Y/N)
    @Column(name = "kospi100_issu_yn", length = 1)
    private String kospi100Yn;

    // KOSPI50 종목 여부 (Y/N)
    @Column(name = "kospi50_issu_yn", length = 1)
    private String kospi50Yn;

    // KRX 종목 여부 (Y/N)
    @Column(name = "krx_issu_yn", length = 1)
    private String krxYn;

    // KRX100 종목 여부 (Y/N)
    @Column(name = "krx100_issu_yn", length = 1)
    private String krx100Yn;

    // SRI 지수 여부 (사회책임투자 지수, Y/N)
    @Column(name = "sri_nmix_yn", length = 1)
    private String sriIndexYn;

    // KRX300 종목 여부 (Y/N)
    @Column(name = "krx300_issu_yn", length = 1)
    private String krx300Yn;

    // KOSPI 여부 (Y/N)
    @Column(name = "kospi_issu_yn", length = 1)
    private String kospiYn;

    public KospiIndexInfo(long kospiMasterId,
                          String kospi100Yn,
                          String kospi50Yn,
                          String krxYn,
                          String krx100Yn,
                          String sriIndexYn,
                          String krx300Yn,
                          String kospiYn) {
        this.kospiMasterId = kospiMasterId;
        this.kospi100Yn = kospi100Yn;
        this.kospi50Yn = kospi50Yn;
        this.krxYn = krxYn;
        this.krx100Yn = krx100Yn;
        this.sriIndexYn = sriIndexYn;
        this.krx300Yn = krx300Yn;
        this.kospiYn = kospiYn;
    }

    public KospiIndexInfo update(String kospi100Yn,
                                 String kospi50Yn,
                                 String krxYn,
                                 String krx100Yn,
                                 String sriIndexYn,
                                 String krx300Yn,
                                 String kospiYn) {
        this.kospi100Yn = kospi100Yn;
        this.kospi50Yn = kospi50Yn;
        this.krxYn = krxYn;
        this.krx100Yn = krx100Yn;
        this.sriIndexYn = sriIndexYn;
        this.krx300Yn = krx300Yn;
        this.kospiYn = kospiYn;

        return this;
    }
}
