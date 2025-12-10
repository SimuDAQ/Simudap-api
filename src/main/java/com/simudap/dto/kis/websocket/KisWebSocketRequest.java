package com.simudap.dto.kis.websocket;

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
        public static Header of(String approvalKey, String customerType, String transactionType, String contentType) {
            return new Header(approvalKey, customerType, transactionType, contentType);
        }
    }

    public record Body(
            Input input
    ) {
        public static Body of(String transactionId, String transactionKey) {
            return new Body(new Input(transactionId, transactionKey));
        }

        private record Input(
                @JsonProperty("tr_id")
                String transactionId,

                @JsonProperty("tr_key")
                String transactionKey
        ) {
        }
    }
}
