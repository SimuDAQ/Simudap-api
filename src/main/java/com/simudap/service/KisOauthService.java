package com.simudap.service;

import com.simudap.dto.kis_oauth.KisOauthRequest;
import com.simudap.dto.kis_oauth.KisOauthResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class KisOauthService {

    private static final String KIS_DOMAIN = "https://openapi.koreainvestment.com:9443";

    private final RestTemplate restTemplate;

    @Value("${kis.app-key}")
    private String appKey;
    @Value("${kis.app-secret}")
    private String appSecret;

    public KisToken getToken() {
        Optional<KisToken> tokenOpt = kisTokenRepository.findOneByOrderByTokenExpiredDesc();

        // 1. 저장된 토큰이 없을 때
        if (tokenOpt.isEmpty()) {
            KisToken kisToken = getKisToken();
            return kisTokenRepository.save(kisToken);
        }

        // token 이 존재할 경우
        KisToken kisToken = tokenOpt.get();
        LocalDateTime tokenExpired = kisToken.getTokenExpired().minusHours(17);
        LocalDateTime now = LocalDateTime.now();

        // 2. 토큰 갱신 후 옛날 토큰 제거
        if (tokenExpired.isBefore(now)) {
            KisToken updated = getKisToken();
            removeOld(kisToken.getId());
            return updated;
        }

        // 3. 아직 유효하면 그대로 return
        return kisToken;
    }

    private void removeOld(long id) {
        kisTokenRepository.deleteById(id);
    }

    private KisToken getKisToken() {
        KisOauthRequest oauthRequest = new KisOauthRequest(appKey, appSecret);
        String uri = UriComponentsBuilder
                .fromUriString(KIS_DOMAIN)
                .path("/oauth2/tokenP")
                .toUriString();
        try {
            // 한국투자 증권에 요청해서 토큰 발급 받는 부분
            ResponseEntity<KisOauthResponse> response = restTemplate.postForEntity(uri, oauthRequest, KisOauthResponse.class);
            return Optional.ofNullable(response.getBody())
                    .orElseThrow(() -> new RuntimeException("Token is null"));

            // 토큰 저장
            return saveToken(token);
        } catch (Exception e) {
            throw new RuntimeException(e.getCause());
        }
    }

    private KisToken saveToken(KisOauthResponse token) {
        KisToken kisToken = new KisToken(token.token(), token.getTokenExpired());
        return kisTokenRepository.save(kisToken);
    }

    private record KisOauthResponse(
            @JsonProperty("access_token")
            String token,

            @JsonProperty("token_type")
            String tokenType,

            @JsonProperty("expires_in")
            Long expiresIn,

            @JsonProperty("access_token_token_expired")
            String tokenExpired
    ) {
        public LocalDateTime getTokenExpired() {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return LocalDateTime.parse(tokenExpired, formatter);
        }
    }

}
