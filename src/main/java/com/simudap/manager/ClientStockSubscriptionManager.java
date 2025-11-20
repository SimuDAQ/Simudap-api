package com.simudap.manager;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

/**
 * STOMP 기반 종목 구독 관리 서비스
 * 종목별 구독자 세션 ID를 추적하여 KIS 구독 관리에 활용
 */
@Slf4j
@Service
public class ClientStockSubscriptionManager {

    // 종목 코드별로 구독 중인 세션 ID들을 관리
    private final Map<String, Set<String>> stockSubscriptions = new ConcurrentHashMap<>();

    // 세션 ID별로 구독 중인 종목 코드들을 관리 (역인덱스)
    private final Map<String, Set<String>> sessionSubscriptions = new ConcurrentHashMap<>();

    /**
     * 구독자 추가
     */
    public void addSubscriber(String stockCode, String sessionId) {
        stockSubscriptions.computeIfAbsent(stockCode, k -> new CopyOnWriteArraySet<>()).add(sessionId);
        sessionSubscriptions.computeIfAbsent(sessionId, k -> new CopyOnWriteArraySet<>()).add(stockCode);
        log.info("Subscriber added - Stock: {}, Session: {}", stockCode, sessionId);
    }

    /**
     * 구독자 제거
     */
    public void removeSubscriber(String stockCode, String sessionId) {
        Set<String> subscribers = stockSubscriptions.get(stockCode);
        if (subscribers != null) {
            subscribers.remove(sessionId);
            if (subscribers.isEmpty()) {
                stockSubscriptions.remove(stockCode);
            }
        }

        Set<String> subscriptions = sessionSubscriptions.get(sessionId);
        if (subscriptions != null) {
            subscriptions.remove(stockCode);
            if (subscriptions.isEmpty()) {
                sessionSubscriptions.remove(sessionId);
            }
        }

        log.info("Subscriber removed - Stock: {}, Session: {}", stockCode, sessionId);
    }

    /**
     * 특정 세션의 모든 구독 제거
     */
    public void removeAllSubscriptions(String sessionId) {
        Set<String> subscriptions = sessionSubscriptions.remove(sessionId);
        if (subscriptions != null) {
            subscriptions.forEach(stockCode -> {
                Set<String> subscribers = stockSubscriptions.get(stockCode);
                if (subscribers != null) {
                    subscribers.remove(sessionId);
                    if (subscribers.isEmpty()) {
                        stockSubscriptions.remove(stockCode);
                    }
                }
            });
            log.info("All subscriptions removed for session: {}", sessionId);
        }
    }

    /**
     * 특정 종목의 구독자가 있는지 확인
     */
    public boolean hasSubscribers(String stockCode) {
        Set<String> subscribers = stockSubscriptions.get(stockCode);
        return subscribers != null && !subscribers.isEmpty();
    }

    /**
     * 구독자가 없는 종목 코드들 조회
     */
    public Set<String> getStocksWithoutSubscribers() {
        return stockSubscriptions.entrySet().stream()
                .filter(entry -> entry.getValue().isEmpty())
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }
}