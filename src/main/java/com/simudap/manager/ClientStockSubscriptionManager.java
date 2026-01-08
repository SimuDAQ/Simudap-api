package com.simudap.manager;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ClientStockSubscriptionManager {

    // 종목 코드별로 구독 중인 세션 ID들을 관리
    private final Map<String, Set<String>> stockSubscriptions = new ConcurrentHashMap<>();

    // 세션 ID별로 구독 중인 종목 코드들을 관리 (역인덱스)
    private final Map<String, Set<String>> sessionSubscriptions = new ConcurrentHashMap<>();

    public void addSubscriber(String stockCode, String sessionId) {
        stockSubscriptions.computeIfAbsent(stockCode, k -> new CopyOnWriteArraySet<>()).add(sessionId);
        sessionSubscriptions.computeIfAbsent(sessionId, k -> new CopyOnWriteArraySet<>()).add(stockCode);
    }

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
    }

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
        }
    }

    public boolean hasSubscribers(String stockCode) {
        Set<String> subscribers = stockSubscriptions.get(stockCode);
        return subscribers != null && !subscribers.isEmpty();
    }

    public Set<String> getStocksWithoutSubscribers() {
        return stockSubscriptions.entrySet().stream()
                .filter(entry -> entry.getValue().isEmpty())
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }
}