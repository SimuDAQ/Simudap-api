package com.simudap.controller;

import com.simudap.dto.kis_websocket.ClientMessage;
import com.simudap.dto.websocket.WebSocketResponse;
import com.simudap.facade.WebSocketFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

/**
 * STOMP WebSocket 메시지 컨트롤러
 * 클라이언트의 구독/구독 해제 요청을 처리
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class StockWebSocketController {


    private final WebSocketFacade webSocketFacade;

    /**
     * 클라이언트가 종목 구독 요청
     * 메시지: {"action": "subscribe", "stockCode": "005930"}
     * 응답: /user/queue/reply
     */
    @MessageMapping("/stock/subscribe")
    @SendToUser("/queue/reply")
    public WebSocketResponse subscribe(@Payload ClientMessage message, SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        String stockCode = message.stockCode();

        webSocketFacade.subscribe(sessionId, stockCode);

        log.info("Stock subscription completed - Session: {}, Stock: {}", sessionId, stockCode);
        return WebSocketResponse.success("subscribed", stockCode);
    }

    /**
     * 클라이언트가 종목 구독 해제 요청
     * 메시지: {"action": "unsubscribe", "stockCode": "005930"}
     * 응답: /user/queue/reply
     */
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