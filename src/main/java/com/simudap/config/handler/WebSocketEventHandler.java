package com.simudap.config.handler;

import com.simudap.manager.ClientStockSubscriptionManager;
import com.simudap.service.KisWebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

/**
 * WebSocket 이벤트 핸들러
 * STOMP 세션의 구독/구독 해제 이벤트를 처리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventHandler {

    private final ClientStockSubscriptionManager subscriptionManager;
    private final KisWebSocketService kisWebSocketService;

    /**
     * STOMP 구독 해제 이벤트 처리
     * 클라이언트가 연결을 끊거나 구독을 취소할 때 호출
     */
    @EventListener
    public void handleUnsubscribeEvent(SessionUnsubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        log.info("STOMP unsubscribe event - Session: {}", sessionId);

        // 해당 세션의 모든 구독 제거
        subscriptionManager.removeAllSubscriptions(sessionId);

        // 구독자가 없는 종목은 KIS 구독 해제
        for (String stockCode : subscriptionManager.getStocksWithoutSubscribers()) {
            if (kisWebSocketService.isSubscribed(stockCode)) {
                kisWebSocketService.unsubscribe(stockCode);
            }
        }
    }
}
