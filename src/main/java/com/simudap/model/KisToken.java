package com.simudap.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(schema = "simudaq")
@Getter
@NoArgsConstructor
public class KisToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, length = 2000)
    private String token;

    @Column(nullable = false)
    private LocalDateTime tokenExpired;

    public KisToken(String token, LocalDateTime tokenExpired) {
        this.token = token;
        this.tokenExpired = tokenExpired;
    }

    public void updateToken(String token, LocalDateTime tokenExpired) {
        this.token = token;
        this.tokenExpired = tokenExpired;
    }
}
