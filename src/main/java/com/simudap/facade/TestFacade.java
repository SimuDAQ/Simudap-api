package com.simudap.facade;

import com.simudap.model.KisToken;
import com.simudap.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TestFacade {

    private final TokenService tokenService;

    public void test() {
        KisToken token = tokenService.getKisToken();
    }
}
