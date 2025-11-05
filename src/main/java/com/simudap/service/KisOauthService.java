package com.simudap.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.simudap.dto.KisOauthRequest;
import com.simudap.model.KisToken;
import com.simudap.repository.KisTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

// 예를 들어 현재가를 찾는 API 호출이 들어왔어
// TradeController 에서 TradeService getCurrentPrice() -> 먼저 kis 토큰을 우리 DB 에서 찾는다
// TimeStamp 기준으로 가장 최근거 하나만 꺼내옴 Repository Optional<KisToken> findOne
// 1. 없으면 여기 서비스 호출해서 토큰 발급하고 저장하고 조회해서 가져옴
// 2. 있어 그럼 만료시간 먼저 체크를 함 -> 7시간이 경과된 토큰이면 새로 재발급 해서 저장하고 이전거는 삭제 그리고 새로운 토큰을 return 함

@Service
@RequiredArgsConstructor
public class KisOauthService {

    private static final String KIS_DOMAIN = "https://openapi.koreainvestment.com:9443";
    private final KisTokenRepository kisTokenRepository;
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
            KisOauthResponse token = Optional.ofNullable(response.getBody())
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
