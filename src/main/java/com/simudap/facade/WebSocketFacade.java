package com.simudap.facade;

import com.simudap.manager.ClientStockSubscriptionManager;
import com.simudap.service.KisWebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WebSocketFacade {

    private final KisWebSocketService kisWebSocketService;
    private final ClientStockSubscriptionManager clientStockSubscriptionManager;

    public void subscribe(String sessionId, String stockCode) {
        // 구독자 등록
        clientStockSubscriptionManager.addSubscriber(stockCode, sessionId);

        // KIS WebSocket에 구독 요청
        kisWebSocketService.subscribe(stockCode);
    }

    public void unsubscribe(String sessionId, String stockCode) {
        // 구독자 제거
        clientStockSubscriptionManager.removeSubscriber(stockCode, sessionId);

        // 더 이상 구독자가 없으면 KIS에도 구독 해제 요청
        if (!clientStockSubscriptionManager.hasSubscribers(stockCode)) {
            kisWebSocketService.unsubscribe(stockCode);
        }
    }
}
