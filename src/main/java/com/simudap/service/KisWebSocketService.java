package com.simudap.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simudap.dto.kis.websocket.KisWebSocketRequest;
import com.simudap.enums.kis.KisTradeType;
import com.simudap.enums.kis.KisRequestHeader;
import com.simudap.error.WebSocketOperationException;
import com.simudap.manager.KisWebSocketConnectionManager;
import com.simudap.model.KisToken;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class KisWebSocketService {

    private final TokenService tokenService;
    private final KisWebSocketConnectionManager connectionManager;
    private final ObjectMapper objectMapper;

    // 현재 KIS에 구독 요청한 종목 코드들
    private final Set<String> subscribedStocks = ConcurrentHashMap.newKeySet();
    // KIS WebSocket 세션 (ConnectionManager에서 콜백으로 설정됨)
    private WebSocketSession kisSession;

    @PostConstruct
    public void init() {
        // 세션 설정 콜백 등록
        connectionManager.setSessionSetterCallback(this::setKisSession);

        // 연결 성공 시 재구독 콜백 등록
        connectionManager.setOnConnectionEstablishedCallback(this::onConnectionEstablished);

        // 구독자가 없을 때 KIS 구독 해제 콜백 등록
        connectionManager.setUnsubscribeIfNoSubscribersCallback(this::unsubscribeIfNoSubscribers);
    }

    private void setKisSession(WebSocketSession session) {
        this.kisSession = session;
        log.info("KIS WebSocket session set: {}", session.getId());
    }

    public void subscribe(String stockCode) {
        // KIS WebSocket 연결 확인 및 필요시 연결 (ConnectionManager에 위임)
        // Supplier를 전달하여 최신 세션 값을 항상 체크하도록 함
        connectionManager.ensureConnected(() -> kisSession);

        if (isSubscribed(stockCode)) {
            return;
        }

        try {
            KisWebSocketRequest askBidRequest = createSubscribeRequest(stockCode, KisTradeType.ASK_BID);
            String askBidJsonRequest = objectMapper.writeValueAsString(askBidRequest);
            kisSession.sendMessage(new TextMessage(askBidJsonRequest));

            KisWebSocketRequest executionRequest = createSubscribeRequest(stockCode, KisTradeType.EXECUTION);
            String executionJsonRequest = objectMapper.writeValueAsString(executionRequest);
            kisSession.sendMessage(new TextMessage(executionJsonRequest));

            subscribedStocks.add(stockCode);
        } catch (Exception e) {
            throw new WebSocketOperationException("An error occurred while processing the stock subscription.");
        }
    }

    public void unsubscribe(String stockCode) {
        if (kisSession == null || !kisSession.isOpen()) {
            log.error("KIS WebSocket session is not connected");
            return;
        }

        if (!subscribedStocks.contains(stockCode)) {
            return;
        }

        try {
            KisWebSocketRequest askBidRequest = createUnsubscribeRequest(stockCode, KisTradeType.ASK_BID);
            String askBidJsonRequest = objectMapper.writeValueAsString(askBidRequest);
            kisSession.sendMessage(new TextMessage(askBidJsonRequest));

            KisWebSocketRequest executionRequest = createUnsubscribeRequest(stockCode, KisTradeType.EXECUTION);
            String executionJsonRequest = objectMapper.writeValueAsString(executionRequest);
            kisSession.sendMessage(new TextMessage(executionJsonRequest));

            subscribedStocks.remove(stockCode);
        } catch (Exception e) {
            throw new WebSocketOperationException("An error occurred while processing the stock unsubscription.");
        }
    }

    public boolean isSubscribed(String stockCode) {
        return subscribedStocks.contains(stockCode);
    }

    private void onConnectionEstablished() {
        // 기존 구독 종목 재구독
        resubscribeAll();
    }

    private void unsubscribeIfNoSubscribers(String stockCode) {
        try {
            unsubscribe(stockCode);
        } catch (Exception e) {
            log.error("KIS unsubscribe failed - Stock code: {}", stockCode, e);
        }
    }

    private void resubscribeAll() {
        if (subscribedStocks.isEmpty()) {
            return;
        }

        // 기존 구독 종목 복사 (동시성 문제 방지)
        Set<String> stocksToResubscribe = ConcurrentHashMap.newKeySet();
        stocksToResubscribe.addAll(subscribedStocks);

        // 구독 목록 초기화
        subscribedStocks.clear();

        // 각 종목 재구독 (ensureConnected 호출하지 않고 직접 처리)
        for (String stockCode : stocksToResubscribe) {
            try {
                if (kisSession == null || !kisSession.isOpen()) {
                    log.warn("KIS WebSocket session is not open, stopping resubscription");
                    break;
                }

                KisWebSocketRequest askBidRequest = createSubscribeRequest(stockCode, KisTradeType.ASK_BID);
                String askBidJsonRequest = objectMapper.writeValueAsString(askBidRequest);
                kisSession.sendMessage(new TextMessage(askBidJsonRequest));

                KisWebSocketRequest executionRequest = createSubscribeRequest(stockCode, KisTradeType.EXECUTION);
                String executionJsonRequest = objectMapper.writeValueAsString(executionRequest);
                kisSession.sendMessage(new TextMessage(executionJsonRequest));

                subscribedStocks.add(stockCode);
            } catch (Exception e) {
                log.error("Stock resubscription failed: {}", stockCode, e);
            }
        }
    }

    private KisWebSocketRequest createSubscribeRequest(String stockCode, KisTradeType tradeId) {
        KisToken token = tokenService.getKisToken();

        KisWebSocketRequest.Header header = KisWebSocketRequest.Header.of(
                token.getWebSocketToken(),
                KisRequestHeader.CUSTOMER_TYPE_P.getValue(),
                KisRequestHeader.TRADE_TYPE_REGISTRATION.getValue(),
                StandardCharsets.UTF_8.name()
        );

        KisWebSocketRequest.Body body = KisWebSocketRequest.Body.of(
                tradeId.getValue(),
                stockCode
        );

        return new KisWebSocketRequest(header, body);
    }

    private KisWebSocketRequest createUnsubscribeRequest(String stockCode, KisTradeType tradeIdType) {
        KisToken token = tokenService.getKisToken();

        KisWebSocketRequest.Header header = KisWebSocketRequest.Header.of(
                token.getWebSocketToken(),  // approval_key
                KisRequestHeader.CUSTOMER_TYPE_P.getValue(),
                KisRequestHeader.TRADE_TYPE_UNREGISTRATION.getValue(),
                StandardCharsets.UTF_8.name()
        );

        KisWebSocketRequest.Body body = KisWebSocketRequest.Body.of(
                tradeIdType.getValue(),
                stockCode
        );

        return new KisWebSocketRequest(header, body);
    }
}