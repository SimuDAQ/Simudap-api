package com.simudap.enums.kis;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum KisTradeType {
    KRX("H0STASP0", "KRX"),
    NXT("H0NXASP0", "NXT"),
    ASK_BID("H0UNASP0", "실시간 호가"),
    EXECUTION("H0STCNT0", "실시간 체결"),
    ;

    private final String value;
    private final String description;
}
