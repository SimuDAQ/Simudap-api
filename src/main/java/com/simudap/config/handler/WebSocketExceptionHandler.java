package com.simudap.config.handler;

import com.simudap.dto.websocket.WebSocketResponse;
import com.simudap.error.WebSocketOperationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.ControllerAdvice;

/**
 * WebSocket 전역 예외 처리기
 * Controller에서 발생한 예외를 중앙에서 처리
 */
@Slf4j
@ControllerAdvice
public class WebSocketExceptionHandler {

    /**
     * WebSocket 작업 예외 처리 (구독/구독해제 등)
     */
    @MessageExceptionHandler(WebSocketOperationException.class)
    @SendToUser("/queue/reply")
    public WebSocketResponse handleWebSocketOperationException(WebSocketOperationException exception) {
        log.error(exception.getMessage(), exception);
        return WebSocketResponse.error(exception.getMessage());
    }

    /**
     * IllegalArgumentException 처리
     */
    @MessageExceptionHandler(IllegalArgumentException.class)
    @SendToUser("/queue/reply")
    public WebSocketResponse handleIllegalArgumentException(IllegalArgumentException exception) {
        log.warn(exception.getMessage(), exception);
        return WebSocketResponse.error(exception.getMessage());
    }

    /**
     * IllegalStateException 처리
     */
    @MessageExceptionHandler(IllegalStateException.class)
    @SendToUser("/queue/reply")
    public WebSocketResponse handleIllegalStateException(IllegalStateException exception) {
        log.warn(exception.getMessage(), exception);
        return WebSocketResponse.error(exception.getMessage());
    }

    /**
     * 모든 예외를 처리하여 클라이언트에게 에러 응답 전송
     */
    @MessageExceptionHandler
    @SendToUser("/queue/reply")
    public WebSocketResponse handleException(Exception exception) {
        log.error("WebSocket error occurred: {}", exception.getMessage(), exception);
        return WebSocketResponse.error("Unexpected error: " + exception.getMessage());
    }
}