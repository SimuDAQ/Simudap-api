package com.simudap.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.simudap.service.KisWebSocketService;
import com.simudap.service.WebSocketSessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClientWebSocketHandler extends TextWebSocketHandler {

    private final WebSocketSessionManager sessionManager;
    private final KisWebSocketService kisWebSocketService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessionManager.addSession(session);
        log.info("클라이언트 연결됨: {}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.info("클라이언트로부터 메시지 수신: {}", payload);

        try {
            // JSON 파싱: {"action": "subscribe", "stockCode": "005930"}
            JsonNode jsonNode = objectMapper.readTree(payload);
            String action = jsonNode.get("action").asText();
            String stockCode = jsonNode.get("stockCode").asText();

            switch (action) {
                case "subscribe":
                    handleSubscribe(session, stockCode);
                    break;
                case "unsubscribe":
                    handleUnsubscribe(session, stockCode);
                    break;
                default:
                    log.warn("알 수 없는 액션: {}", action);
            }
        } catch (Exception e) {
            log.error("메시지 처리 중 오류 발생", e);
            session.sendMessage(new TextMessage("{\"error\": \"Invalid message format\"}"));
        }
    }

    private void handleSubscribe(WebSocketSession session, String stockCode) throws Exception {
        // 세션 매니저에 구독 등록
        sessionManager.subscribe(stockCode, session);

        // KIS WebSocket에 구독 요청 (첫 구독자인 경우에만)
        if (!kisWebSocketService.isSubscribed(stockCode)) {
            kisWebSocketService.subscribe(stockCode);
        }

        // 구독 성공 응답
        String response = String.format("{\"status\": \"subscribed\", \"stockCode\": \"%s\"}", stockCode);
        session.sendMessage(new TextMessage(response));
        log.info("종목 구독 완료 - 세션: {}, 종목: {}", session.getId(), stockCode);
    }

    private void handleUnsubscribe(WebSocketSession session, String stockCode) throws Exception {
        // 세션 매니저에서 구독 해제
        sessionManager.unsubscribe(stockCode, session);

        // 더 이상 구독자가 없으면 KIS에도 구독 해제 요청
        if (!sessionManager.hasSubscribers(stockCode)) {
            kisWebSocketService.unsubscribe(stockCode);
        }

        // 구독 해제 성공 응답
        String response = String.format("{\"status\": \"unsubscribed\", \"stockCode\": \"%s\"}", stockCode);
        session.sendMessage(new TextMessage(response));
        log.info("종목 구독 해제 완료 - 세션: {}, 종목: {}", session.getId(), stockCode);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessionManager.removeSession(session);
        log.info("클라이언트 연결 해제됨: {}, 상태: {}", session.getId(), status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket 전송 오류 - 세션: {}", session.getId(), exception);
        sessionManager.removeSession(session);
    }
}