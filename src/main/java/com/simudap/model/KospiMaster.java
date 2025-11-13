package com.simudap.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(schema = "simudaq")
@Getter
@NoArgsConstructor
public class KospiMaster {

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

    public KospiMaster(String shortCode, String standardCode, String nameKr) {
        this.shortCode = shortCode;
        this.standardCode = standardCode;
        this.nameKr = nameKr;
    }

    public KospiMaster update(String shortCode, String standardCode, String nameKr) {
        this.shortCode = shortCode;
        this.standardCode = standardCode;
        this.nameKr = nameKr;

        return this;
    }
}
