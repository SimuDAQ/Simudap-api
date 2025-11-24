package com.simudap.config.handler;

import com.simudap.dto.websocket.WebSocketResponse;
import com.simudap.error.WebSocketOperationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.ControllerAdvice;

@Slf4j
@ControllerAdvice
public class WebSocketExceptionHandler {

    @MessageExceptionHandler(WebSocketOperationException.class)
    @SendToUser("/queue/reply")
    public WebSocketResponse handleWebSocketOperationException(WebSocketOperationException exception) {
        log.error(exception.getMessage(), exception);
        return WebSocketResponse.error(exception.getMessage());
    }

    @MessageExceptionHandler(IllegalArgumentException.class)
    @SendToUser("/queue/reply")
    public WebSocketResponse handleIllegalArgumentException(IllegalArgumentException exception) {
        log.warn(exception.getMessage(), exception);
        return WebSocketResponse.error(exception.getMessage());
    }

    @MessageExceptionHandler(IllegalStateException.class)
    @SendToUser("/queue/reply")
    public WebSocketResponse handleIllegalStateException(IllegalStateException exception) {
        log.warn(exception.getMessage(), exception);
        return WebSocketResponse.error(exception.getMessage());
    }

    @MessageExceptionHandler
    @SendToUser("/queue/reply")
    public WebSocketResponse handleException(Exception exception) {
        log.error("WebSocket error occurred: {}", exception.getMessage(), exception);
        return WebSocketResponse.error("Unexpected error: " + exception.getMessage());
    }
}