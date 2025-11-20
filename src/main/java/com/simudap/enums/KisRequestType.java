package com.simudap.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum KisRequestType {
    // Header
    CUSTOMER_TYPE_P("P", "개인"),
    TRADE_TYPE_REGISTRATION("1", "등록"),
    TRADE_TYPE_UNREGISTRATION("2", "해제"),

    // Body
    TRADE_ID_KRX("H0STASP0", "KRX"),
    TRADE_ID_NXT("H0NXASP0", "KRX"),
    TRADE_ID_INTEGRATION("H0UNASP0", "KRX"),
    ;

    private final String value;
    private final String description;
}
