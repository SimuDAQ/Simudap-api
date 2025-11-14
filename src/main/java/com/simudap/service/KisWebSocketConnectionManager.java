package com.simudap.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketConnectionManager;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * KIS WebSocket 연결 관리 서비스
 * 연결, 재연결, 연결 상태 확인 책임을 담당
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KisWebSocketConnectionManager {

    private final WebSocketSessionManager sessionManager;
    private final ObjectMapper objectMapper = new ObjectMapper();
    // 재연결 관련
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final AtomicInteger reconnectAttempts = new AtomicInteger(0);
    private WebSocketConnectionManager connectionManager;
    /**
     * -- SETTER --
     * KisWebSocketService에서 콜백 등록
     */
    // 연결 성공 시 실행할 콜백 (KisWebSocketService에서 등록)
    @Setter
    private Runnable onConnectionEstablishedCallback;
    /**
     * -- SETTER --
     * KisWebSocketService에서 세션 설정 콜백 등록
     */
    // 세션 설정 콜백
    @Setter
    private Consumer<WebSocketSession> sessionSetterCallback;
    /**
     * -- SETTER --
     * 구독자가 없을 때 KIS 구독 해제 콜백
     */
    // 구독자가 없을 때 KIS에 구독 해제 요청 콜백
    @Setter
    private Consumer<String> unsubscribeIfNoSubscribersCallback;
    @Value("${kis.websocket.url}")
    private String kisWebSocketUrl;

    @Value("${kis.websocket.max-reconnect-attempts}")
    private int maxReconnectAttempts;

    @Value("${kis.websocket.initial-reconnect-delay}")
    private long initialReconnectDelay;

    @Value("${kis.websocket.max-reconnect-delay}")
    private long maxReconnectDelay;

    @PostConstruct
    public void init() {
        // WebSocketConnectionManager 생성 (자동 시작 안 함)
        StandardWebSocketClient webSocketClient = new StandardWebSocketClient();

        // 내부 Handler 생성 (Bean이 아님, 순환 의존성 방지)
        BinaryWebSocketHandler handler = new BinaryWebSocketHandler() {
            @Override
            public void afterConnectionEstablished(WebSocketSession session) {
                log.info("KIS WebSocket connected: {}", session.getId());

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
        log.info("KIS WebSocket ConnectionManager initialization completed");
    }

    /**
     * KIS WebSocket이 연결되어 있는지 확인
     */
    private boolean isConnected() {
        return connectionManager != null && connectionManager.isRunning();
    }

    /**
     * KIS WebSocket 연결 시작
     */
    private void connect() {
        if (connectionManager == null) {
            log.error("ConnectionManager is not initialized.");
            return;
        }

        if (!connectionManager.isRunning()) {
            log.info("KIS WebSocket connection start...");
            connectionManager.start();
        } else {
            log.info("KIS WebSocket is already connected.");
        }
    }

    /**
     * KIS WebSocket 연결 종료
     */
    public void disconnect() {
        if (connectionManager != null && connectionManager.isRunning()) {
            log.info("KIS WebSocket connection closing...");
            connectionManager.stop();
        }
    }

    /**
     * 연결 성공 시 호출 (내부 Handler에서 호출)
     */
    private void onConnectionEstablished() {
        log.info("KIS WebSocket connection successful");
        // 재연결 시도 횟수 초기화
        reconnectAttempts.set(0);

        // 콜백 실행 (재구독 등)
        if (onConnectionEstablishedCallback != null) {
            onConnectionEstablishedCallback.run();
        }
    }

    /**
     * 재연결 스케줄링 (지수 백오프 적용)
     */
    public void scheduleReconnect() {
        int currentAttempt = reconnectAttempts.incrementAndGet();

        if (currentAttempt > maxReconnectAttempts) {
            log.error("KIS WebSocket exceeded maximum reconnect attempts ({}). Stopping reconnection.", maxReconnectAttempts);
            return;
        }

        // 지수 백오프 계산: initialDelay * 2^(attempt-1)
        long delay = Math.min(
                initialReconnectDelay * (long) Math.pow(2, currentAttempt - 1),
                maxReconnectDelay
        );

        log.info("KIS WebSocket reconnect attempt {}/{} - Retrying after {}ms",
                currentAttempt, maxReconnectAttempts, delay);

        scheduler.schedule(this::reconnect, delay, TimeUnit.MILLISECONDS);
    }

    /**
     * KIS WebSocket 재연결 수행
     */
    private void reconnect() {
        try {
            log.info("KIS WebSocket reconnecting...");

            // 기존 연결 종료
            if (connectionManager.isRunning()) {
                connectionManager.stop();
            }

            // 새로운 연결 시작
            connectionManager.start();

            log.info("KIS WebSocket reconnection request completed");
        } catch (Exception e) {
            log.error("Error occurred during KIS WebSocket reconnection", e);
            // 재연결 실패 시 다시 스케줄링
            scheduleReconnect();
        }
    }

    /**
     * 연결이 안 되어 있으면 연결하고 대기
     */
    public void ensureConnected(Supplier<WebSocketSession> sessionSupplier) {
        WebSocketSession session = sessionSupplier.get();

        if (session == null || !session.isOpen()) {
            log.info("KIS WebSocket is not connected. Connecting start...");

            if (!isConnected()) {
                connect();

                // 연결이 완료될 때까지 대기 (최대 5초)
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

    /**
     * KIS로부터 받은 메시지 처리
     */
    private void handleKisMessage(TextMessage message) {
        String[] stockInfos = message.getPayload().split("\\^");
        if (stockInfos.length > 1) {
            String stockShortCode = stockInfos[0];
            String[] split = stockShortCode.split("\\|");
            String stockCode = split[split.length - 1];

            // Log output (for debugging)
            log.info("Stock short code: {}", stockShortCode);
            log.info("Business time: {}, Time code: {}", stockInfos[1], stockInfos[2]);

            // 구독자가 있는지 확인
            if (!sessionManager.hasSubscribers(stockCode)) {
                log.warn("No subscribers for stock code {}. Requesting KIS unsubscribe...", stockCode);

                // 구독자가 없으면 KIS에 구독 해제 요청
                if (unsubscribeIfNoSubscribersCallback != null) {
                    unsubscribeIfNoSubscribersCallback.accept(stockCode);
                }
                return;  // 메시지 처리 중단
            }

            // 데이터를 JSON 형태로 변환
            Map<String, Object> data = buildStockData(stockInfos);

            try {
                String jsonData = objectMapper.writeValueAsString(data);
                // 해당 종목을 구독 중인 클라이언트들에게 브로드캐스트
                sessionManager.broadcastToSubscriber(stockCode, jsonData);
                log.debug("Stock data transmission completed: {}", stockShortCode);
            } catch (Exception e) {
                log.error("Error occurred during data transmission - Stock code: {}", stockShortCode, e);
            }
        }
    }

    /**
     * 수신한 데이터를 구조화된 JSON 객체로 변환
     */
    private Map<String, Object> buildStockData(String[] recvvalue) {
        Map<String, Object> data = new HashMap<>();

        data.put("stockCode", recvvalue[0]);
        data.put("businessTime", recvvalue[1]);
        data.put("timeCode", recvvalue[2]);

        // 매도호가 (10단계)
        Map<String, Object> askPrices = new HashMap<>();
        for (int i = 1; i <= 10; i++) {
            Map<String, String> priceInfo = new HashMap<>();
            priceInfo.put("price", recvvalue[12 - i + 1]);
            priceInfo.put("volume", recvvalue[32 - i + 1]);
            askPrices.put("level" + i, priceInfo);
        }
        data.put("askPrices", askPrices);

        // 매수호가 (10단계)
        Map<String, Object> bidPrices = new HashMap<>();
        for (int i = 1; i <= 10; i++) {
            Map<String, String> priceInfo = new HashMap<>();
            priceInfo.put("price", recvvalue[12 + i]);
            priceInfo.put("volume", recvvalue[32 + i]);
            bidPrices.put("level" + i, priceInfo);
        }
        data.put("bidPrices", bidPrices);

        // 총 호가 정보
        data.put("totalAskVolume", recvvalue[43]);
        data.put("totalAskVolumeChange", recvvalue[54]);
        data.put("totalBidVolume", recvvalue[44]);
        data.put("totalBidVolumeChange", recvvalue[55]);

        // 시간외 호가 정보
        data.put("afterHoursTotalAskVolume", recvvalue[45]);
        data.put("afterHoursTotalBidVolume", recvvalue[46]);
        data.put("afterHoursTotalAskVolumeChange", recvvalue[56]);
        data.put("afterHoursTotalBidVolumeChange", recvvalue[57]);

        // 예상 체결 정보
        data.put("expectedPrice", recvvalue[47]);
        data.put("expectedVolume", recvvalue[48]);
        data.put("expectedTotalVolume", recvvalue[49]);
        data.put("expectedPriceChange", recvvalue[50]);
        data.put("expectedPriceSign", recvvalue[51]);
        data.put("expectedPriceChangeRate", recvvalue[52]);

        // 기타
        data.put("accumulatedVolume", recvvalue[53]);
        data.put("tradeTypeCode", recvvalue[58]);

        return data;
    }
}