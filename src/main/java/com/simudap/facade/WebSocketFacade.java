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
        clientStockSubscriptionManager.addSubscriber(stockCode, sessionId);
        kisWebSocketService.subscribe(stockCode);
    }

    public void unsubscribe(String sessionId, String stockCode) {
        clientStockSubscriptionManager.removeSubscriber(stockCode, sessionId);

        if (!clientStockSubscriptionManager.hasSubscribers(stockCode)) {
            kisWebSocketService.unsubscribe(stockCode);
        }
    }
}
