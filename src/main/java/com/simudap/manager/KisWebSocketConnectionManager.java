package com.simudap.manager;

import com.simudap.dto.websocket.StockRealtimeData;
import com.simudap.dto.websocket.StockRealtimeData.ExpectedTrade;
import com.simudap.dto.websocket.StockRealtimeData.PriceLevel;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketConnectionManager;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import java.util.ArrayList;
import java.util.List;
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

    private final ClientStockSubscriptionManager subscriptionManager;
    private final SimpMessagingTemplate messagingTemplate;

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
            if (!subscriptionManager.hasSubscribers(stockCode)) {
                log.warn("No subscribers for stock code {}. Requesting KIS unsubscribe...", stockCode);

                // 구독자가 없으면 KIS에 구독 해제 요청
                if (unsubscribeIfNoSubscribersCallback != null) {
                    unsubscribeIfNoSubscribersCallback.accept(stockCode);
                }
                return;  // 메시지 처리 중단
            }

            // 데이터를 DTO로 변환
            StockRealtimeData data = buildStockData(stockInfos);

            try {
                // STOMP를 통해 해당 종목을 구독 중인 클라이언트들에게 브로드캐스트
                messagingTemplate.convertAndSend("/topic/stock/" + stockCode, data);
                log.debug("Stock data transmission completed: {}", stockShortCode);
            } catch (Exception e) {
                log.error("Error occurred during data transmission - Stock code: {}", stockShortCode, e);
            }
        }
    }

    /**
     * 수신한 데이터를 StockRealtimeData DTO로 변환
     */
    private StockRealtimeData buildStockData(String[] recvvalue) {
        // 매도호가 (10단계)
        List<PriceLevel> askPrices = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            askPrices.add(new PriceLevel(
                    recvvalue[12 - i + 1],  // price
                    recvvalue[32 - i + 1]   // volume
            ));
        }

        // 매수호가 (10단계)
        List<PriceLevel> bidPrices = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            bidPrices.add(new PriceLevel(
                    recvvalue[12 + i],  // price
                    recvvalue[32 + i]   // volume
            ));
        }

        // 예상 체결 정보
        ExpectedTrade expectedTrade = new ExpectedTrade(
                recvvalue[47],  // price
                recvvalue[48],  // volume
                recvvalue[49],  // totalVolume
                recvvalue[50],  // priceChange
                recvvalue[51],  // priceSign
                recvvalue[52]   // priceChangeRate
        );

        return new StockRealtimeData(
                recvvalue[0],   // stockCode
                recvvalue[1],   // businessTime
                recvvalue[2],   // timeCode
                askPrices,
                bidPrices,
                recvvalue[43],  // totalAskVolume
                recvvalue[54],  // totalAskVolumeChange
                recvvalue[44],  // totalBidVolume
                recvvalue[55],  // totalBidVolumeChange
                recvvalue[45],  // afterHoursTotalAskVolume
                recvvalue[46],  // afterHoursTotalBidVolume
                recvvalue[56],  // afterHoursTotalAskVolumeChange
                recvvalue[57],  // afterHoursTotalBidVolumeChange
                expectedTrade,
                recvvalue[53],  // accumulatedVolume
                recvvalue[58]   // tradeTypeCode
        );
    }
}