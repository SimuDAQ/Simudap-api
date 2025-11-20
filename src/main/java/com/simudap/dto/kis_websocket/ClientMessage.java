package com.simudap.dto.kis_websocket;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.simudap.enums.WebsocketAction;

public record ClientMessage(
        WebsocketAction action,
        String stockCode
) {
    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public static ClientMessage of(@JsonProperty("action") String action, @JsonProperty("stockCode") String stockCode) {
        return new ClientMessage(WebsocketAction.from(action), stockCode);
    }
}
