package com.simudap.dto.websocket;

public record WebSocketResponse(
        String status,
        String message,
        String stockCode
) {
    public static WebSocketResponse success(String status, String stockCode) {
        return new WebSocketResponse(status, null, stockCode);
    }

    public static WebSocketResponse error(String message) {
        return new WebSocketResponse("error", message, null);
    }
}