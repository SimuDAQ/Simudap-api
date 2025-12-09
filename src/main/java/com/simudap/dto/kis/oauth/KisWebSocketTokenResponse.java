package com.simudap.dto.kis.oauth;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KisWebSocketTokenResponse(
        @JsonProperty("approval_key")
        String approvalKey
) {
}
