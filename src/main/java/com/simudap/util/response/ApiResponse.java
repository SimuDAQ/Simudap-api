package com.simudap.util.response;

public record ApiResponse<T>(
        T data,
        Error error
) {
    public static <R> ApiResponse<R> of(R data) {
        return new ApiResponse<>(data, null);
    }

    public static ApiResponse<Void> error(Error error) {
        return new ApiResponse<>(null, error);
    }
}
