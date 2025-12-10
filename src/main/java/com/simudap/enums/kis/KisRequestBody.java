package com.simudap.enums.kis;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum KisRequestBody {
    TRADE_ID_KRX("H0STASP0", "KRX"),
    TRADE_ID_NXT("H0NXASP0", "NXT"),
    TRADE_ID_UN("H0UNASP0", "통합"),
    ;

    private final String value;
    private final String description;
}
