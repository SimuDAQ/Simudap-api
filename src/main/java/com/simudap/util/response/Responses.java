package com.simudap.util.response;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class Responses {
    public static ResponseEntity<ApiResponse<Void>> error(Error error) {
        ApiResponse<Void> root = ApiResponse.error(error);
        return ResponseEntity.status(error.getStatus()).body(root);
    }

    public static <T> ResponseEntity<ApiResponse<T>> ok(T body) {
        ApiResponse<T> root = ApiResponse.of(body);
        return ResponseEntity.ok(root);
    }

    public static <T> ResponseEntity<ApiResponse<ListResponse<T, Void>>> ok(List<T> list) {
        ListResponse<T, Void> response = ListResponse.of(list);
        return ok(response);
    }

    public static <T> ResponseEntity<ApiResponse<PageResponse<T, Void>>> ok(Page<T> page) {
        PageResponse<T, Void> response = PageResponse.of(page);
        return ok(response);
    }

    public static ResponseEntity<Void> ok() {
        return ResponseEntity.ok().build();
    }

    public static ResponseEntity<Void> noContent() {
        return ResponseEntity.noContent().build();
    }
}
