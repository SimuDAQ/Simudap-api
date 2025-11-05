package com.simudap.controller;

import com.simudap.facade.TestFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TestController {

    private final TestFacade testFacade;

    @GetMapping("/test")
    public void test() {
        testFacade.test();
    }
}
