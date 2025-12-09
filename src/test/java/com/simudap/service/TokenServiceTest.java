package com.simudap.service;

import com.simudap.dto.kis.oauth.KisTokenInfo;
import com.simudap.model.KisToken;
import com.simudap.repository.KisTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @Mock
    private KisApiService kisApiService;

    @Mock
    private KisTokenRepository kisTokenRepository;

    @InjectMocks
    private TokenService tokenService;

    private KisTokenInfo mockTokenInfo;
    private KisToken mockKisToken;
    private LocalDateTime futureExpireTime;
    private LocalDateTime nearExpireTime;
    private DateTimeFormatter formatter;

    @BeforeEach
    void setUp() {
        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // 24시간 후 만료 (발급 직후, 갱신 불필요)
        futureExpireTime = LocalDateTime.now().plusHours(24);

        // 11시간 후 만료 (발급 후 13시간 경과, 재발급 필요)
        nearExpireTime = LocalDateTime.now().plusHours(11);

        mockTokenInfo = new KisTokenInfo(
                "mock-rest-api-token",
                "mock-websocket-token",
                futureExpireTime.format(formatter)
        );

        mockKisToken = new KisToken(
                "existing-rest-api-token",
                "existing-websocket-token",
                futureExpireTime
        );
    }

    @Test
    @DisplayName("DB에 토큰이 없을 때 새로운 토큰을 발급받아 저장한다")
    void testGetKisToken_WhenNoTokenExists_ShouldCreateNewToken() {
        // given
        when(kisTokenRepository.findTop1ByOrderByTokenExpiredDesc())
                .thenReturn(Optional.empty());
        when(kisApiService.getToken()).thenReturn(mockTokenInfo);
        when(kisTokenRepository.save(any(KisToken.class))).thenAnswer(invocation -> invocation.<KisToken>getArgument(0));

        // when
        KisToken result = tokenService.getKisToken();

        // then
        assertThat(result).isNotNull();
        assertThat(result.getRestApiToken()).isEqualTo(mockTokenInfo.restApiToken());
        assertThat(result.getWebSocketToken()).isEqualTo(mockTokenInfo.webSocketToken());
        assertThat(result.getTokenExpired()).isEqualTo(mockTokenInfo.getTokenExpired());

        verify(kisTokenRepository).findTop1ByOrderByTokenExpiredDesc();
        verify(kisApiService).getToken();
        verify(kisTokenRepository).save(any(KisToken.class));
    }

    @Test
    @DisplayName("유효한 토큰이 있을 때 기존 토큰을 반환한다")
    void testGetKisToken_WhenValidTokenExists_ShouldReturnExistingToken() {
        // given
        KisToken validToken = new KisToken(
                "existing-rest-api-token",
                "existing-websocket-token",
                futureExpireTime
        );
        when(kisTokenRepository.findTop1ByOrderByTokenExpiredDesc())
                .thenReturn(Optional.of(validToken));

        // when
        KisToken result = tokenService.getKisToken();

        // then
        assertThat(result).isNotNull();
        assertThat(result.getRestApiToken()).isEqualTo("existing-rest-api-token");
        assertThat(result.getWebSocketToken()).isEqualTo("existing-websocket-token");
        assertThat(result.getTokenExpired()).isEqualTo(futureExpireTime);

        verify(kisTokenRepository).findTop1ByOrderByTokenExpiredDesc();
        verify(kisApiService, never()).getToken();
        verify(kisTokenRepository, never()).save(any(KisToken.class));
    }

    @Test
    @DisplayName("만료 임박한 토큰이 있을 때 토큰을 갱신한다")
    void testGetKisToken_WhenTokenExpiringSoon_ShouldUpdateToken() {
        // given
        KisToken expiringSoonToken = new KisToken(
                "old-rest-api-token",
                "old-websocket-token",
                nearExpireTime
        );

        KisTokenInfo newTokenInfo = new KisTokenInfo(
                "new-rest-api-token",
                "new-websocket-token",
                LocalDateTime.now().plusHours(24).format(formatter)
        );

        when(kisTokenRepository.findTop1ByOrderByTokenExpiredDesc())
                .thenReturn(Optional.of(expiringSoonToken));
        when(kisApiService.getToken()).thenReturn(newTokenInfo);

        // when
        KisToken result = tokenService.getKisToken();

        // then
        assertThat(result).isNotNull();
        assertThat(result.getRestApiToken()).isEqualTo("new-rest-api-token");
        assertThat(result.getWebSocketToken()).isEqualTo("new-websocket-token");
        assertThat(result.getTokenExpired()).isEqualTo(newTokenInfo.getTokenExpired());

        verify(kisTokenRepository).findTop1ByOrderByTokenExpiredDesc();
        verify(kisApiService).getToken();
        // updateToken 메서드가 호출되므로 save는 호출되지 않음 (@Transactional에 의해 자동 저장)
        verify(kisTokenRepository, never()).save(any(KisToken.class));
    }

    @Test
    @DisplayName("만료 정확히 12시간 전 토큰은 갱신하지 않는다")
    void testGetKisToken_WhenTokenExpires12HoursLater_ShouldNotUpdate() {
        // given
        LocalDateTime exactly12HoursLater = LocalDateTime.now().plusHours(12).plusMinutes(1);
        KisToken tokenExpires12Hours = new KisToken(
                "existing-rest-api-token",
                "existing-websocket-token",
                exactly12HoursLater
        );

        when(kisTokenRepository.findTop1ByOrderByTokenExpiredDesc())
                .thenReturn(Optional.of(tokenExpires12Hours));

        // when
        KisToken result = tokenService.getKisToken();

        // then
        assertThat(result).isNotNull();
        assertThat(result.getRestApiToken()).isEqualTo("existing-rest-api-token");
        assertThat(result.getWebSocketToken()).isEqualTo("existing-websocket-token");

        verify(kisTokenRepository).findTop1ByOrderByTokenExpiredDesc();
        verify(kisApiService, never()).getToken();
    }

    @Test
    @DisplayName("만료 11시간 59분 후 토큰은 갱신한다")
    void testGetKisToken_WhenTokenExpiresJustUnder12Hours_ShouldUpdate() {
        // given
        LocalDateTime justUnder12Hours = LocalDateTime.now().plusHours(11).plusMinutes(59);
        KisToken tokenExpiresJustUnder12Hours = new KisToken(
                "old-rest-api-token",
                "old-websocket-token",
                justUnder12Hours
        );

        KisTokenInfo newTokenInfo = new KisTokenInfo(
                "new-rest-api-token",
                "new-websocket-token",
                LocalDateTime.now().plusHours(24).format(formatter)
        );

        when(kisTokenRepository.findTop1ByOrderByTokenExpiredDesc())
                .thenReturn(Optional.of(tokenExpiresJustUnder12Hours));
        when(kisApiService.getToken()).thenReturn(newTokenInfo);

        // when
        KisToken result = tokenService.getKisToken();

        // then
        assertThat(result).isNotNull();
        assertThat(result.getRestApiToken()).isEqualTo("new-rest-api-token");
        assertThat(result.getWebSocketToken()).isEqualTo("new-websocket-token");

        verify(kisTokenRepository).findTop1ByOrderByTokenExpiredDesc();
        verify(kisApiService).getToken();
    }
}