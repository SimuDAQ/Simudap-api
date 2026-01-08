package com.simudap.controller;

import com.simudap.dto.kis.websocket.ClientMessage;
import com.simudap.dto.websocket.WebSocketResponse;
import com.simudap.facade.WebSocketFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class StockWebSocketController {

    private final WebSocketFacade webSocketFacade;

    @MessageMapping("/stock/subscribe")
    @SendToUser("/queue/reply")
    public WebSocketResponse subscribe(@Payload ClientMessage message, SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        String stockCode = message.stockCode();

        webSocketFacade.subscribe(sessionId, stockCode);

        log.info("Stock subscription completed - Session: {}, Stock: {}", sessionId, stockCode);

        return WebSocketResponse.success("subscribed", stockCode);
    }

    @MessageMapping("/stock/unsubscribe")
    @SendToUser("/queue/reply")
    public WebSocketResponse unsubscribe(@Payload ClientMessage message, SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        String stockCode = message.stockCode();

        webSocketFacade.unsubscribe(sessionId, stockCode);

        log.info("Stock unsubscription completed - Session: {}, Stock: {}", sessionId, stockCode);
        return WebSocketResponse.success("unsubscribed", stockCode);
    }
}