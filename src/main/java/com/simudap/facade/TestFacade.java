package com.simudap.facade;

import com.simudap.model.KisToken;
import com.simudap.service.KisOauthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TestFacade {

    private final KisOauthService kisOauthService;

    public void test() {
        KisToken token = kisOauthService.getToken();

    }
}
