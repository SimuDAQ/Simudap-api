package com.simudap.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class ExternalApiCallException extends RuntimeException {
    public ExternalApiCallException(String message) {
        super(message);
    }
}
