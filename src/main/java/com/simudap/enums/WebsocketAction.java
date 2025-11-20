package com.simudap.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum WebsocketAction {
    SUBSCRIBE("subscribe"),
    UNSUBSCRIBE("unsubscribe"),
    ;

    private final String value;

    public static WebsocketAction from(String value) {
        return Arrays.stream(WebsocketAction.values())
                .filter(action -> action.value.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid websocket action value: " + value));
    }
}
