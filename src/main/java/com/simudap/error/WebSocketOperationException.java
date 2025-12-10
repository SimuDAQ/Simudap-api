package com.simudap.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
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