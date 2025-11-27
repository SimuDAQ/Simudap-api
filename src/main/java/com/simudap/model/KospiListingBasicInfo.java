package com.simudap.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(schema = "simudaq")
public class KospiListingBasicInfo {    // 상장, 자본, 기본 정보

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    // 외래키
    @Column(nullable = false)
    private long kospiMasterId;

    // 주식 액면가
    @Column(name = "stck_fcam", length = 12)
    private Integer parValue;

    // 상장 일자 (YYYYMMDD)
    @Column(name = "stck_lstn_date", length = 8)
    private LocalDateTime listingDate;

    // 상장 주수(천 단위)
    @Column(name = "lstn_stcn", length = 15)
    private Integer listedSharesThousand;

    // 자본금
    @Column(name = "cpfn", length = 21)
    private Long capital;

    // 결산 월
    @Column(name = "stac_month", length = 2)
    private Integer settlementMonth;

    // 공모가
    @Column(name = "po_prc", length = 7)
    private Integer publicOfferingPrice;

    // 그룹사 코드
    @Column(name = "grp_code", length = 3)
    private String groupCode;

    public KospiListingBasicInfo(long kospiMasterId,
                                 Integer parValue,
                                 LocalDateTime listingDate,
                                 Integer listedSharesThousand,
                                 Long capital,
                                 Integer settlementMonth,
                                 Integer publicOfferingPrice,
                                 String groupCode) {
        this.kospiMasterId = kospiMasterId;
        this.parValue = parValue;
        this.listingDate = listingDate;
        this.listedSharesThousand = listedSharesThousand;
        this.capital = capital;
        this.settlementMonth = settlementMonth;
        this.publicOfferingPrice = publicOfferingPrice;
        this.groupCode = groupCode;
    }

    public KospiListingBasicInfo update(Integer parValue,
                                        LocalDateTime listingDate,
                                        Integer listedSharesThousand,
                                        Long capital,
                                        Integer settlementMonth,
                                        Integer publicOfferingPrice,
                                        String groupCode) {
        this.parValue = parValue;
        this.listingDate = listingDate;
        this.listedSharesThousand = listedSharesThousand;
        this.capital = capital;
        this.settlementMonth = settlementMonth;
        this.publicOfferingPrice = publicOfferingPrice;
        this.groupCode = groupCode;

        return this;
    }
}
