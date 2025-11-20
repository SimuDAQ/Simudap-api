package com.simudap.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * STOMP WebSocket 설정
 * Pub/Sub 방식의 메시지 브로커를 사용하여 실시간 주식 데이터 전송
 * KIS WebSocket 연결은 KisWebSocketConnectionManager에서 관리
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 클라이언트로 메시지를 전송할 때 사용할 prefix
        // 예: /topic/stock/{stockCode}
        config.enableSimpleBroker("/topic");

        // 클라이언트에서 메시지를 보낼 때 사용할 prefix
        // 예: /app/subscribe
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // STOMP 엔드포인트 등록
        registry.addEndpoint("/ws/stock")
                .setAllowedOrigins("*")  // CORS 설정 (프로덕션에서는 특정 도메인으로 제한 필요)
                .withSockJS();  // SockJS fallback 옵션 활성화
    }
}
