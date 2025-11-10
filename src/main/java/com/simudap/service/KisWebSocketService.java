package com.simudap.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simudap.dto.kis_websocket.KisWebSocketRequest;
import com.simudap.model.KisToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class KisWebSocketService {

    private final TokenService tokenService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    // 현재 KIS에 구독 요청한 종목 코드들
    private final Set<String> subscribedStocks = ConcurrentHashMap.newKeySet();
    // KIS WebSocket 세션 (WebSocketMessageHandler에서 설정됨)
    private WebSocketSession kisSession;

    /**
     * KIS WebSocket 세션 설정
     */
    public void setKisSession(WebSocketSession session) {
        this.kisSession = session;
        log.info("KIS WebSocket 세션 설정됨: {}", session.getId());
    }

    /**
     * 종목 구독 요청
     */
    public void subscribe(String stockCode) throws IOException {
        if (kisSession == null || !kisSession.isOpen()) {
            log.error("KIS WebSocket 세션이 연결되지 않음");
            throw new IllegalStateException("KIS WebSocket not connected");
        }

        // 이미 구독 중이면 무시
        if (subscribedStocks.contains(stockCode)) {
            log.info("이미 구독 중인 종목: {}", stockCode);
            return;
        }

        // 구독 요청 생성
        KisWebSocketRequest request = createSubscribeRequest(stockCode);
        String jsonRequest = objectMapper.writeValueAsString(request);

        // KIS에 구독 요청 전송
        kisSession.sendMessage(new TextMessage(jsonRequest));
        subscribedStocks.add(stockCode);

        log.info("KIS 구독 요청 전송: {}", stockCode);
    }

    /**
     * 종목 구독 해제 요청
     */
    public void unsubscribe(String stockCode) throws IOException {
        if (kisSession == null || !kisSession.isOpen()) {
            log.error("KIS WebSocket 세션이 연결되지 않음");
            return;
        }

        if (!subscribedStocks.contains(stockCode)) {
            log.info("구독 중이지 않은 종목: {}", stockCode);
            return;
        }

        // 구독 해제 요청 생성
        KisWebSocketRequest request = createUnsubscribeRequest(stockCode);
        String jsonRequest = objectMapper.writeValueAsString(request);

        // KIS에 구독 해제 요청 전송
        kisSession.sendMessage(new TextMessage(jsonRequest));
        subscribedStocks.remove(stockCode);

        log.info("KIS 구독 해제 요청 전송: {}", stockCode);
    }

    /**
     * 특정 종목이 구독 중인지 확인
     */
    public boolean isSubscribed(String stockCode) {
        return subscribedStocks.contains(stockCode);
    }

    /**
     * 구독 요청 생성
     */
    private KisWebSocketRequest createSubscribeRequest(String stockCode) {
        KisToken token = tokenService.getKisToken();

        KisWebSocketRequest.Header header = new KisWebSocketRequest.Header(
                token.getWebSocketToken(),  // approval_key
                "P",                         // custtype (개인: P)
                "1",                         // tr_type (등록: 1)
                "utf-8"                      // content-type
        );

        KisWebSocketRequest.Body body = new KisWebSocketRequest.Body(
                "H0STASP0",                  // tr_id (실시간 호가)
                stockCode                    // tr_key (종목 코드)
        );

        return new KisWebSocketRequest(header, body);
    }

    /**
     * 구독 해제 요청 생성
     */
    private KisWebSocketRequest createUnsubscribeRequest(String stockCode) {
        KisToken token = tokenService.getKisToken();

        KisWebSocketRequest.Header header = new KisWebSocketRequest.Header(
                token.getWebSocketToken(),  // approval_key
                "P",                         // custtype (개인: P)
                "2",                         // tr_type (해제: 2)
                "utf-8"                      // content-type
        );

        KisWebSocketRequest.Body body = new KisWebSocketRequest.Body(
                "H0STASP0",                  // tr_id (실시간 호가)
                stockCode                    // tr_key (종목 코드)
        );

        return new KisWebSocketRequest(header, body);
    }
}