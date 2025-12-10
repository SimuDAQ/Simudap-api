package com.simudap.enums.kis;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;

@Getter
@RequiredArgsConstructor
public enum KisRequestHeader {
    CONTENT_TYPE_UTF8("content-type", MediaType.APPLICATION_JSON_VALUE, "컨텐츠 타입"),
    AUTHORIZATION("authorization", null, "접근토큰"),
    APP_KEY("appkey", null, "앱키"),
    APP_SECRET("appsecret", null, "앱시크릿"),
    TRADE_ID("tr_id", null, "거래 ID"),
    CUSTOMER_TYPE_P("custtype", "P", "커스토머 타입(개인)"),
    TRADE_TYPE_REGISTRATION("tr_type", "1", "등록"),
    TRADE_TYPE_UNREGISTRATION("tr_type", "2", "해제"),
    ;

    private final String key;
    private final String value;
    private final String description;
}
