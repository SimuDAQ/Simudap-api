package com.simudap.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(schema = "simudaq")
@NoArgsConstructor
public class KospiIndustryInfo {    // 업종(산업) 대/중/소 분류

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    // 외래키
    @Column(nullable = false)
    private long kospiMasterId;

    // 지수 업종 대분류 코드
    @Column(name = "bstp_larg_div_code", length = 4)
    private Integer industryLargeCode;

    // 지수 업종 중분류 코드
    @Column(name = "bstp_medm_div_code", length = 4)
    private Integer industryMidCode;

    // 지수 업종 소분류 코드
    @Column(name = "bstp_smal_div_code", length = 4)
    private Integer industrySmallCode;

    public KospiIndustryInfo(Long kospiMasterId,
                             Integer industryLargeCode,
                             Integer industryMidCode,
                             Integer industrySmallCode) {
        this.kospiMasterId = kospiMasterId;
        this.industryLargeCode = industryLargeCode;
        this.industryMidCode = industryMidCode;
        this.industrySmallCode = industrySmallCode;
    }

    public KospiIndustryInfo update(Integer industryLargeCode,
                                    Integer industryMidCode,
                                    Integer industrySmallCode) {
        this.industryLargeCode = industryLargeCode;
        this.industryMidCode = industryMidCode;
        this.industrySmallCode = industrySmallCode;

        return this;
    }
}
