package com.simudap.dto.websocket;

/**
 * WebSocket 응답 DTO
 * 클라이언트에게 전송되는 응답 메시지 형식
 */
public record WebSocketResponse(
        String status,
        String message,
        String stockCode,
        String dataEndpoint  // 클라이언트가 구독해야 할 실시간 데이터 엔드포인트
) {
    /**
     * 성공 응답 생성 (dataEndpoint 포함)
     */
    public static WebSocketResponse success(String status, String stockCode, String dataEndpoint) {
        return new WebSocketResponse(status, null, stockCode, dataEndpoint);
    }

    /**
     * 성공 응답 생성 (dataEndpoint 없음)
     */
    public static WebSocketResponse success(String status, String stockCode) {
        return new WebSocketResponse(status, null, stockCode, null);
    }

    /**
     * 에러 응답 생성
     */
    public static WebSocketResponse error(String message) {
        return new WebSocketResponse("error", message, null, null);
    }
}