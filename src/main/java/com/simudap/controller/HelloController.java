package com.simudap.controller;

import com.simudap.service.KisOauthService;
import com.simudap.service.SchedulerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class HelloController {

    private final SchedulerService schedulerService;

    @GetMapping("/hello")
    public String hello() {
        return "Hello";
    }

    @GetMapping("/download")
    public void testDownload() throws IOException {
        schedulerService.downloadKospiFile();
    }
}
