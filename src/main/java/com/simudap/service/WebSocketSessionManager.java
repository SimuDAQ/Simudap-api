package com.simudap.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Slf4j
@Service
public class WebSocketSessionManager {

    // 종목 코드별로 구독 중인 클라이언트 세션들을 관리
    private final Map<String, Set<WebSocketSession>> stockSubscriptions = new ConcurrentHashMap<>();

    // 모든 연결된 클라이언트 세션
    private final Set<WebSocketSession> clientSessions = new CopyOnWriteArraySet<>();

    /**
     * 클라이언트 세션 추가
     */
    public void addSession(WebSocketSession session) {
        clientSessions.add(session);
        log.info("Client session added: {}", session.getId());
    }

    /**
     * 클라이언트 세션 제거
     */
    public void removeSession(WebSocketSession session) {
        clientSessions.remove(session);
        // 해당 세션이 구독 중인 모든 종목에서 제거
        stockSubscriptions.values().forEach(sessions -> sessions.remove(session));
        log.info("Client session removed: {}", session.getId());
    }

    /**
     * 특정 종목 코드 구독
     */
    public void subscribe(String stockCode, WebSocketSession session) {
        stockSubscriptions.computeIfAbsent(stockCode, k -> new CopyOnWriteArraySet<>()).add(session);
        log.info("Stock subscription: {} by sessionId {}", stockCode, session.getId());
    }

    /**
     * 특정 종목 코드 구독 해제
     */
    public void unsubscribe(String stockCode, WebSocketSession session) {
        Set<WebSocketSession> sessions = stockSubscriptions.get(stockCode);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                stockSubscriptions.remove(stockCode);
            }
            log.info("Stock unsubscribed: {} by session {}", stockCode, session.getId());
        }
    }

    /**
     * 특정 종목을 구독 중인 모든 클라이언트에게 메시지 브로드캐스트
     */
    public void broadcastToSubscriber(String stockCode, String message) {
        Set<WebSocketSession> sessions = stockSubscriptions.get(stockCode);
        if (sessions == null || sessions.isEmpty()) {
            return;
        }

        TextMessage textMessage = new TextMessage(message);
        for (WebSocketSession session : sessions) {
            if (!session.isOpen()) {
                // client 세션이 끊겨 있으면 구독 해제 후 메세지 전송시도 x
                unsubscribe(stockCode, session);
                continue;
            }

            try {
                session.sendMessage(textMessage);
            } catch (IOException e) {
                log.error("Failed to send message - Session: {}, Stock code: {}", session.getId(), stockCode, e);
            }
        }
    }

    /**
     * 모든 클라이언트에게 메시지 브로드캐스트
     */
    public void broadcastToAll(String message) {
        TextMessage textMessage = new TextMessage(message);
        clientSessions.forEach(session -> {
            if (session.isOpen()) {
                try {
                    session.sendMessage(textMessage);
                } catch (IOException e) {
                    log.error("Failed to send message - Session: {}", session.getId(), e);
                }
            }
        });
    }

    /**
     * 특정 종목을 구독 중인 세션이 있는지 확인
     */
    public boolean hasSubscribers(String stockCode) {
        Set<WebSocketSession> sessions = stockSubscriptions.get(stockCode);
        return sessions != null && !sessions.isEmpty();
    }

    /**
     * 구독 중인 모든 종목 코드 조회
     */
    public Set<String> getSubscribedStocks() {
        return stockSubscriptions.keySet();
    }
}