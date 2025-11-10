package com.simudap.dto.kis_websocket;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KisWebSocketRequest(
        Header header,
        Body body
) {
    public record Header(
            @JsonProperty("approval_key")
            String approvalKey,

            @JsonProperty("custtype")
            String customerType,

            @JsonProperty("tr_type")
            String transactionType,

            @JsonProperty("content-type")
            String contentType
    ) {
    }

    public record Body(
            @JsonProperty("tr_id")
            String transactionId,

            @JsonProperty("tr_key")
            String transactionKey
    ) {
    }
}
