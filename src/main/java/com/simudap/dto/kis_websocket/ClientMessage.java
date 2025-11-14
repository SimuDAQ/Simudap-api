package com.simudap.dto.kis_websocket;

import com.simudap.enums.WebsocketAction;

public record ClientMessage(
        WebsocketAction action,
        String stockCode
) {
    public ClientMessage(String action, String stockCode) {
        this(WebsocketAction.from(action), stockCode);
    }
}
