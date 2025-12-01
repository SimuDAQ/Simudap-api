package com.simudap.model.kospi;

import com.simudap.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(schema = "simudaq")
@Getter
@NoArgsConstructor
public class KospiMaster extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    // 단축코드
    @Column(nullable = false, unique = true)
    private String shortCode;

    // 표준코드
    @Column(nullable = false, unique = true)
    private String standardCode;

    // 한글종목명
    @Column(nullable = false)
    private String nameKr;

    @Column(nullable = false)
    private boolean isDeListed = false;

    public KospiMaster(String shortCode, String standardCode, String nameKr, boolean isDeListed) {
        this.shortCode = shortCode;
        this.standardCode = standardCode;
        this.nameKr = nameKr;
        this.isDeListed = isDeListed;
    }

    public KospiMaster update(String shortCode, String standardCode, String nameKr, boolean isDeListed) {
        this.shortCode = shortCode;
        this.standardCode = standardCode;
        this.nameKr = nameKr;
        this.isDeListed = isDeListed;

        return this;
    }

    public void updateStatus(boolean isDeListed) {
        this.isDeListed = isDeListed;
    }
}
