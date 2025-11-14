package com.simudap.config;


import com.simudap.handler.ClientWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket 설정
 * 클라이언트가 연결할 WebSocket 엔드포인트만 등록
 * KIS WebSocket 연결은 KisWebSocketConnectionManager에서 관리
 */
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final ClientWebSocketHandler clientWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 클라이언트가 연결할 WebSocket 엔드포인트 등록
        registry.addHandler(clientWebSocketHandler, "/ws/stock")
                .setAllowedOrigins("*");  // CORS 설정 (프로덕션에서는 특정 도메인으로 제한 필요)
    }
}
