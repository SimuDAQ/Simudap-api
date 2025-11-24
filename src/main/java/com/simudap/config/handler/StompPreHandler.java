package com.simudap.config.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * STOMP 구독 인터셉터
 * 세션별 구독 정보를 추적하여 중복 구독을 차단
 */
@Slf4j
@Component
public class StompPreHandler implements ChannelInterceptor {

    private final Map<String, Set<String>> sessionSubscriptions = new ConcurrentHashMap<>();

    private final Map<String, String> subscriptionIdToDestination = new ConcurrentHashMap<>();

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(
                message,
                StompHeaderAccessor.class
        );

        if (accessor != null) {
            StompCommand command = accessor.getCommand();
            String sessionId = accessor.getSessionId();

            if (StompCommand.SUBSCRIBE.equals(command)) {
                String destination = accessor.getDestination();
                String subscriptionId = accessor.getSubscriptionId();

                log.debug("SUBSCRIBE command - Session: {}, Destination: {}, SubId: {}",
                         sessionId, destination, subscriptionId);

                if (isAlreadySubscribed(sessionId, destination)) {
                    log.warn("Duplicate subscription blocked - Session: {}, Destination: {}",
                            sessionId, destination);
                    return null;
                }

                addSubscription(sessionId, destination, subscriptionId);
                log.info("✅ New subscription allowed - Session: {}, Destination: {}",
                        sessionId, destination);
            } else if (StompCommand.UNSUBSCRIBE.equals(command)) {
                String subscriptionId = accessor.getSubscriptionId();
                removeSubscription(sessionId, subscriptionId);
                log.debug("UNSUBSCRIBE - Session: {}, SubId: {}", sessionId, subscriptionId);
            } else if (StompCommand.DISCONNECT.equals(command)) {
                removeAllSubscriptions(sessionId);
                log.info("DISCONNECT - Removed all subscriptions for session: {}", sessionId);
            }
        }

        return message;
    }

    private boolean isAlreadySubscribed(String sessionId, String destination) {
        Set<String> destinations = sessionSubscriptions.get(sessionId);
        return destinations != null && destinations.contains(destination);
    }

    private void addSubscription(String sessionId, String destination, String subscriptionId) {
        sessionSubscriptions
                .computeIfAbsent(sessionId, k -> new CopyOnWriteArraySet<>())
                .add(destination);

        if (subscriptionId != null) {
            subscriptionIdToDestination.put(subscriptionId, destination);
        }
    }

    private void removeSubscription(String sessionId, String subscriptionId) {
        if (subscriptionId == null) {
            return;
        }

        String destination = subscriptionIdToDestination.remove(subscriptionId);
        if (destination != null) {
            Set<String> destinations = sessionSubscriptions.get(sessionId);
            if (destinations != null) {
                destinations.remove(destination);

                if (destinations.isEmpty()) {
                    sessionSubscriptions.remove(sessionId);
                }
            }
        }
    }

    private void removeAllSubscriptions(String sessionId) {
        Set<String> removed = sessionSubscriptions.remove(sessionId);
        if (removed != null) {
            log.debug("Removed {} subscriptions for session: {}", removed.size(), sessionId);
        }
    }
}
