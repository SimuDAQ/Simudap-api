package com.simudap.manager;

import com.simudap.dto.websocket.StockAskBidData;
import com.simudap.dto.websocket.StockExecutionData;
import com.simudap.enums.kis.KisTradeType;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketConnectionManager;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Slf4j
@Service
@RequiredArgsConstructor
public class KisWebSocketConnectionManager {

    private final ClientStockSubscriptionManager subscriptionManager;
    private final SimpMessagingTemplate messagingTemplate;

    // 재연결 관련
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final AtomicInteger reconnectAttempts = new AtomicInteger(0);
    private WebSocketConnectionManager connectionManager;

    // 연결 성공 시 실행할 콜백 (KisWebSocketService에서 등록)
    @Setter
    private Runnable onConnectionEstablishedCallback;

    // 세션 설정 콜백
    @Setter
    private Consumer<WebSocketSession> sessionSetterCallback;

    // 구독자가 없을 때 KIS에 구독 해제 요청 콜백
    @Setter
    private Consumer<String> unsubscribeIfNoSubscribersCallback;

    @Value("${kis.websocket.url}")
    private String kisWebSocketUrl;

    @Value("${kis.websocket.max-reconnect-attempts}")
    private int maxReconnectAttempts;

    @Value("${kis.websocket.reconnect-delay}")
    private long reconnectDelay;

    @Value("${websocket.endpoints.stock-askbid-topic}")
    private String stockAskBidTopicPrefix;

    @Value("${websocket.endpoints.stock-execution-topic}")
    private String stockExecutionTopicPrefix;

    @PostConstruct
    public void init() {
        // WebSocketConnectionManager 생성 (자동 시작 안 함)
        StandardWebSocketClient webSocketClient = new StandardWebSocketClient();

        // 내부 Handler 생성 (Bean이 아님, 순환 의존성 방지)
        BinaryWebSocketHandler handler = new BinaryWebSocketHandler() {
            @Override
            public void afterConnectionEstablished(WebSocketSession session) {

                // 세션 설정 콜백 실행
                if (sessionSetterCallback != null) {
                    sessionSetterCallback.accept(session);
                }

                // 연결 성공 처리
                onConnectionEstablished();
            }

            @Override
            protected void handleTextMessage(WebSocketSession session, TextMessage message) {
                handleKisMessage(message);
            }

            @Override
            public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
                log.warn("KIS WebSocket disconnected - Reason: {}, Code: {}", status.getReason(), status.getCode());
                if (status.getCode() != CloseStatus.NORMAL.getCode()) {
                    scheduleReconnect();
                }
            }

            @Override
            public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
                log.error("KIS WebSocket transport error occurred", exception);
                scheduleReconnect();
            }
        };

        connectionManager = new WebSocketConnectionManager(
                webSocketClient,
                handler,
                kisWebSocketUrl
        );
        connectionManager.setAutoStartup(false);
    }

    private boolean isConnected() {
        return connectionManager != null && connectionManager.isRunning();
    }

    private void connect() {
        if (connectionManager == null) {
            log.error("ConnectionManager is not initialized.");
            return;
        }

        if (!connectionManager.isRunning()) {
            connectionManager.start();
        }
    }

    private void onConnectionEstablished() {
        // 재연결 시도 횟수 초기화
        reconnectAttempts.set(0);

        // 콜백 실행 (재구독 등)
        if (onConnectionEstablishedCallback != null) {
            onConnectionEstablishedCallback.run();
        }
    }

    public void scheduleReconnect() {
        int currentAttempt = reconnectAttempts.incrementAndGet();

        if (currentAttempt > maxReconnectAttempts) {
            log.error("KIS WebSocket exceeded maximum reconnect attempts ({}). Stopping reconnection.", maxReconnectAttempts);
            return;
        }

        scheduler.schedule(this::reconnect, reconnectDelay, TimeUnit.MILLISECONDS);
    }

    private void reconnect() {
        try {
            // 기존 연결 종료
            if (connectionManager.isRunning()) {
                connectionManager.stop();
            }

            // 새로운 연결 시작
            connectionManager.start();
        } catch (Exception e) {
            log.error("Error occurred during KIS WebSocket reconnection", e);
            // 재연결 실패 시 다시 스케줄링
            scheduleReconnect();
        }
    }


    public void ensureConnected(Supplier<WebSocketSession> sessionSupplier) {
        WebSocketSession session = sessionSupplier.get();

        if (session == null || !session.isOpen()) {
            if (!isConnected()) {
                connect();

                int maxWaitTime = 5000;
                int waitedTime = 0;
                int checkInterval = 100;

                while (waitedTime < maxWaitTime) {
                    try {
                        Thread.sleep(checkInterval);
                        waitedTime += checkInterval;

                        // 매번 최신 세션 값을 체크
                        session = sessionSupplier.get();
                        if (session != null && session.isOpen()) {
                            log.info("KIS WebSocket connection verified");
                            return;
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException("Interrupted while waiting for KIS WebSocket connection", e);
                    }
                }

                throw new IllegalStateException("KIS WebSocket connection timeout");
            }
        }
    }

    private void handleKisMessage(TextMessage message) {
        String[] stockInfos = message.getPayload().split("\\^");
        if (stockInfos.length > 1) {
            String stockShortCode = stockInfos[0];
            String[] split = stockShortCode.split("\\|");

            String trId = split.length > 1 ? split[1] : "";
            String stockCode = split[split.length - 1];

            if (!subscriptionManager.hasSubscribers(stockCode)) {
                log.warn("No subscribers for stock code {}. Requesting KIS unsubscribe...", stockCode);

                if (unsubscribeIfNoSubscribersCallback != null) {
                    unsubscribeIfNoSubscribersCallback.accept(stockCode);
                }
                return;
            }


            if (Strings.CI.equals(KisTradeType.ASK_BID.getValue(), trId)) {
                handleAskBidData(stockCode, stockInfos);
            } else if (Strings.CI.equals(KisTradeType.EXECUTION.getValue(), trId)) {
                handleExecutionData(stockCode, stockInfos);
            } else {
                log.warn("Unknown TR_ID: {}, Stock code: {}", trId, stockCode);
            }
        }
    }

    private void handleAskBidData(String stockCode, String[] stockInfos) {
        try {
            StockAskBidData data = StockAskBidData.of(stockCode, stockInfos);
            String destination = stockAskBidTopicPrefix + stockCode;
            messagingTemplate.convertAndSend(destination, data);
        } catch (Exception e) {
            log.error("Error occurred during askbid data transmission - Stock code: {}", stockCode, e);
        }
    }

    private void handleExecutionData(String stockCode, String[] stockInfos) {
        try {
            StockExecutionData data = StockExecutionData.of(stockCode, stockInfos);
            String destination = stockExecutionTopicPrefix + stockCode;
            messagingTemplate.convertAndSend(destination, data);
        } catch (Exception e) {
            log.error("Error occurred during execution data transmission - Stock code: {}", stockCode, e);
        }
    }
}