package com.simudap.util.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.simudap.util.ErrorUtils;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.OffsetDateTime;
import java.util.Optional;

@Getter
@Setter
public class Error {
    private final OffsetDateTime timestamp = OffsetDateTime.now();
    private final String path;

    @JsonIgnore
    private final HttpStatus httpStatus;

    private String exceptionName;
    private Object bindingErrors;

    private String message = ErrorUtils.DEFAULT_ERROR_MESSAGE;

    private Error(String path, HttpStatusCode status) {
        this.path = path;
        this.httpStatus = Optional.ofNullable(status)
                .map(HttpStatusCode::value)
                .map(HttpStatus::valueOf)
                .orElse(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private Error(String path, HttpStatusCode status, Throwable throwable) {
        this(path, status);

        if (this.httpStatus.is5xxServerError() || throwable == null) {
            // do not set exception detail.
            return;
        }

        this.exceptionName = throwable.getClass().getName();
        this.setErrorDetail(throwable);
    }

    private Error(String path, HttpStatusCode status, String message) {
        this(path, status);

        if (this.httpStatus.is5xxServerError()) {
            // do not set exception detail.
            return;
        }

        if (StringUtils.isNotBlank(message)) {
            this.message = message;
        }
    }

    public static Error of(String path, HttpStatusCode status, Throwable throwable) {
        return new Error(path, status, throwable);
    }

    public static Error of(String path, HttpStatusCode status, String message) {
        return new Error(path, status, message);
    }

    public Integer getStatus() {
        return this.httpStatus.value();
    }

    public String getReason() {
        return this.httpStatus.getReasonPhrase();
    }

    private void setErrorDetail(Throwable error) {
        BindingResult result = extractBindingResult(error);

        if (result != null && result.getErrorCount() > 0) {
            this.bindingErrors = result.getAllErrors();
            this.message = "Validation failed for some input values. Error count: " + result.getErrorCount();
            return;
        }

        this.message = error.getMessage();
    }

    private BindingResult extractBindingResult(Throwable error) {
        if (error instanceof MethodArgumentNotValidException err) {
            return err.getBindingResult();
        }

        if (error instanceof BindingResult err) {
            return err;
        }

        return null;
    }
}
