package com.simudap.dto.kis_websocket;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.simudap.enums.WebsocketAction;

public record ClientMessage(
        String stockCode
) {
}
