package com.simudap.dto.websocket;

/**
 * WebSocket 응답 DTO
 * 클라이언트에게 전송되는 응답 메시지 형식
 */
public record WebSocketResponse(
        String status,
        String message,
        String stockCode
) {
    /**
     * 성공 응답 생성
     */
    public static WebSocketResponse success(String status, String stockCode) {
        return new WebSocketResponse(status, null, stockCode);
    }

    /**
     * 에러 응답 생성
     */
    public static WebSocketResponse error(String message) {
        return new WebSocketResponse("error", message, null);
    }
}