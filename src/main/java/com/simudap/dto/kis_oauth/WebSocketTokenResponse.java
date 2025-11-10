package com.simudap.dto.kis_oauth;

import com.fasterxml.jackson.annotation.JsonProperty;

public record WebSocketTokenResponse(
        @JsonProperty("approval_key")
        String approvalKey
) {
}
