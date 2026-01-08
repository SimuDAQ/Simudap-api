package com.simudap.enums.kis;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum KisTradeType {
    KRX("H0STASP0", "KRX"),
    NXT("H0NXASP0", "NXT"),
    ASK_BID("H0UNASP0", "실시간 호가 통합"),
    EXECUTION("H0UNCNT0", "실시간 체결 통합"),
    ;

    private final String value;
    private final String description;
}
