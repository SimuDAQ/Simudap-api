package com.simudap.controller;

import com.simudap.util.response.ApiResponse;
import com.simudap.util.response.Responses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Hello", description = "Hello API")
@RestController
@RequestMapping("/api")
public class HelloController {

    @Operation(summary = "Hello!")
    @GetMapping("/hello")
    public ResponseEntity<ApiResponse<String>> hello() {
        return Responses.ok("Hello");
    }
}
