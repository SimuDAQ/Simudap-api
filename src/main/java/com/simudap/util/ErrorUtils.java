package com.simudap.util;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.DispatcherServlet;

import java.util.Optional;

@Slf4j
public class ErrorUtils {

    public static final String DEFAULT_ERROR_MESSAGE = "Oops! Something went wrong.";

    public static HttpStatus extractStatus(HttpServletRequest request) {
        Integer statusCode = getAttribute(request, RequestDispatcher.ERROR_STATUS_CODE, Integer.class);
        if (statusCode == null) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }

        try {
            return HttpStatus.valueOf(statusCode);
        } catch (Exception ex) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    public static Throwable extractError(HttpServletRequest request) {
        Throwable error = getAttribute(request, DispatcherServlet.EXCEPTION_ATTRIBUTE, Throwable.class);
        if (error == null) {
            error = getAttribute(request, RequestDispatcher.ERROR_EXCEPTION, Throwable.class);
        }

        if (error == null) {
            return null;
        }

        while (error instanceof ServletException && error.getCause() != null) {
            error = error.getCause();
        }
        return error;
    }

    public static String extractErrorMessage(HttpStatus status, HttpServletRequest request) {
        if (status == HttpStatus.NOT_FOUND) {
            return "Page Not Found.";
        }
        String message = getAttribute(request, RequestDispatcher.ERROR_MESSAGE, String.class);
        return StringUtils.defaultIfBlank(message, DEFAULT_ERROR_MESSAGE);
    }

    public static String extractPath(HttpServletRequest request) {
        return getAttribute(request, RequestDispatcher.ERROR_REQUEST_URI, String.class);
    }

    public static HttpStatus extractHttpStatus(Exception ex) {
        // ResponseStatusException 처리 (Spring 기본 예외)
        if (ex instanceof ResponseStatusException rse) {
            return HttpStatus.valueOf(rse.getStatusCode().value());
        }

        // @ResponseStatus 애너테이션이 있는 경우 해당 HTTP 상태 코드 반환
        if (ex.getClass().isAnnotationPresent(ResponseStatus.class)) {
            return ex.getClass().getAnnotation(ResponseStatus.class).value();
        }

        // 기본적으로 500 (Internal Server Error) 반환
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private static <T> T getAttribute(HttpServletRequest request, String name, Class<T> type) {
        Object attribute = request.getAttribute(name);
        if (type.isInstance(attribute)) {
            return type.cast(attribute);
        }
        return null;
    }

    public static void logError(HttpStatusCode status, HttpServletRequest request, Throwable error) {
        if (request == null) {
            log.error("{} | Request not provided..", error.getMessage(), error);
            return;
        }

        int statusCode = status.value();
        String errorName = error.getClass().getName();
        String errorMessage = error.getMessage();

        String xForwardedFor = request.getHeader("X-Forwarded-For");
        String host = Optional.ofNullable(xForwardedFor)
                .filter(StringUtils::isNotBlank)
                .orElseGet(request::getRemoteHost);

        String userAgent = request.getHeader(HttpHeaders.USER_AGENT);
        String requestUrl = request.getRequestURL().toString();
        String queryString = request.getQueryString();

        String referer = request.getHeader("referer");

        if (status.is5xxServerError()) {
            log.error("status:[ {} ] | exception:[ {} : {} ] | Remote Host:[ {} ] | User Agent:[ {} ] | Request URI:[ {} ] | Request Params:[ {} ] | Referer:[ {} ]",
                    statusCode,
                    errorName,
                    errorMessage,
                    host,
                    userAgent,
                    requestUrl,
                    queryString,
                    referer,
                    error);
        } else {
            log.warn("status:[ {} ] | exception:[ {} : {} ] | Remote Host:[ {} ] | User Agent:[ {} ] | Request URI:[ {} ] | Request Params:[ {} ] | Referer:[ {} ]",
                    statusCode,
                    errorName,
                    errorMessage,
                    host,
                    userAgent,
                    requestUrl,
                    queryString,
                    referer);
        }
    }
}
