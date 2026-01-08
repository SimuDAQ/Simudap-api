package com.simudap.config.handler;

import com.simudap.manager.ClientStockSubscriptionManager;
import com.simudap.service.KisWebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;


@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventHandler {

    private final ClientStockSubscriptionManager subscriptionManager;
    private final KisWebSocketService kisWebSocketService;

    @EventListener
    public void handleUnsubscribeEvent(SessionUnsubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

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
