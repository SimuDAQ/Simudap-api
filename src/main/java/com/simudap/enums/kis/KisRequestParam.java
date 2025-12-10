package com.simudap.enums.kis;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum KisRequestParam {

    // 조건 시장 분류 코드 (J:KRX, NX:NXT, UN:통합)
    FID_COND_MRKT_DIV_CODE("조건 시장 분류 코드(J:KRX, NX:NXT, UN:통합)"),

    FID_INPUT_ISCD("입력 종목 코드(ex 005930)"),
    FID_INPUT_HOUR_1("입력 시간1(ex 13시 130000)"),
    FID_INPUT_DATE_1("입력 날짜1(조회 시작일자 ex 20251210)"),
    FID_INPUT_DATE_2("입력 날짜2(조회 종료일자 ex 20251210)"),
    FID_PERIOD_DIV_CODE("기간분류코드(D:일봉 W:주봉, M:월봉, Y:년봉)"),
    FID_PW_DATA_INCU_YN("과거데이터 포함여부"),
    FID_ETC_CLS_CODE("기타 구분 코드(공백 사용)"),
    FID_FAKE_TICK_INCU_YN("허봉 포함 여부(공백 필수)"),
    FID_ORG_ADJ_PRC("수정주가 원주가 가격 여부(0:수정주가 1:원주가)");

    private final String description;
}
