package com.simudap.service;

import com.simudap.dto.kis_oauth.KisOauthResponse;
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
        KisOauthResponse kisToken = kisOauthService.getKisToken();
        KisToken newOne = new KisToken(kisToken.token(), kisToken.getTokenExpired());
        return kisTokenRepository.save(newOne);
    }

    private KisToken getOrUpdate(KisToken token) {
        LocalDateTime tokenExpired = token.getTokenExpired().minusHours(17);
        LocalDateTime now = LocalDateTime.now();

        // 2. 토큰 갱신 (최근 토큰 발급 시간 7시간 경과 시)
        if (tokenExpired.isBefore(now)) {
            KisOauthResponse newOne = kisOauthService.getKisToken();
            token.updateToken(newOne.token(), newOne.getTokenExpired());
            return token;
        }

        return token;
    }
}
