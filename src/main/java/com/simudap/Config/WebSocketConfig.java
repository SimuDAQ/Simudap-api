package com.simudap.Config;


import com.simudap.handler.ClientWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.client.WebSocketConnectionManager;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;


@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final ClientWebSocketHandler clientWebSocketHandler;
    private final WebSocketMessageHandler kisWebSocketMessageHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 클라이언트가 연결할 WebSocket 엔드포인트 등록
        registry.addHandler(clientWebSocketHandler, "/ws/stock")
                .setAllowedOrigins("*");  // CORS 설정 (프로덕션에서는 특정 도메인으로 제한 필요)
    }

    /**
     * KIS WebSocket 클라이언트 연결 설정
     */
    @Bean
    public WebSocketConnectionManager kisWebSocketConnectionManager() {
        StandardWebSocketClient webSocketClient = new StandardWebSocketClient();

        WebSocketConnectionManager connectionManager = new WebSocketConnectionManager(
                webSocketClient,
                kisWebSocketMessageHandler,
                "ws://ops.koreainvestment.com:21000"
        );

        connectionManager.setAutoStartup(true);
        return connectionManager;
    }
}
