package com.simudap.error;

/**
 * WebSocket 작업(구독/구독해제) 중 발생하는 예외
 */
public class WebSocketOperationException extends RuntimeException {

    public WebSocketOperationException(String message) {
        super(message);
    }

    public WebSocketOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    public WebSocketOperationException(Throwable cause) {
        super(cause);
    }
}