package com.simudap.service;

import com.simudap.dto.kis_oauth.KisOauthResponse;
import com.simudap.model.KisToken;
import com.simudap.repository.KisTokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final KisOauthService kisOauthService;

    private final KisTokenRepository kisTokenRepository;

    @Transactional
    public KisToken getToken() {
        Optional<KisToken> tokenOpt = kisTokenRepository.findOneByOrderByTokenExpiredDesc();

        // 1. 저장된 토큰이 없을 때
        if (tokenOpt.isEmpty()) {
            KisOauthResponse kisToken = kisOauthService.getKisToken();
            KisToken newOne = new KisToken(kisToken.token(), kisToken.getTokenExpired());
            return kisTokenRepository.save(newOne);
        }

        // token 이 존재할 경우
        KisToken kisToken = tokenOpt.get();
        LocalDateTime tokenExpired = kisToken.getTokenExpired().minusHours(17);
        LocalDateTime now = LocalDateTime.now();

        // 2. 토큰 갱신
        if (tokenExpired.isBefore(now)) {
            KisOauthResponse newOne = kisOauthService.getKisToken();
            kisToken.updateToken(newOne.token(), newOne.getTokenExpired());
            return kisToken;
        }

        // 3. 아직 유효하면 그대로 return
        return kisToken;
    }
}
