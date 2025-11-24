package com.simudap.config;

import com.simudap.config.handler.StompPreHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompPreHandler stompPreHandler;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // /topic: 브로드캐스트 (여러 명에게)
        // /queue: 개인 메시지 (특정 사용자에게)
        registry.enableSimpleBroker("/topic", "/queue");

        // 클라이언트에서 메시지를 보낼 때 사용할 prefix
        // 예: /app/subscribe
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/stock")
                .setAllowedOrigins("*");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // 중복 구독 방지 인터셉터 등록
        registration.interceptors(stompPreHandler);
    }
}
