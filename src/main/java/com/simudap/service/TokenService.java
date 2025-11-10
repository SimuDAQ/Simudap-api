package com.simudap.service;

import com.simudap.dto.kis_oauth.KisTokenInfo;
import com.simudap.model.KisToken;
import com.simudap.repository.KisTokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final KisOauthService kisOauthService;

    private final KisTokenRepository kisTokenRepository;

    @Transactional
    public KisToken getKisToken() {
        return kisTokenRepository.findTop1ByOrderByTokenExpiredDesc()
                .map(this::getOrUpdate)
                .orElseGet(this::saveKisToken);
    }

    private KisToken saveKisToken() {
        KisTokenInfo issued = kisOauthService.getToken();
        KisToken newOne = new KisToken(issued.restApiToken(), issued.webSocketToken(), issued.getTokenExpired());
        return kisTokenRepository.save(newOne);
    }

    private KisToken getOrUpdate(KisToken currToken) {
        LocalDateTime expiredAt = currToken.getTokenExpired().minusHours(12);
        LocalDateTime now = LocalDateTime.now();

        // 2. 토큰 갱신 (최근 토큰 발급 시간 12시간 경과 시)
        if (now.isAfter(expiredAt)) {
            KisTokenInfo issued = kisOauthService.getToken();
            currToken.updateToken(issued.restApiToken(), issued.webSocketToken(), issued.getTokenExpired());
            return currToken;
        }

        return currToken;
    }
}
