package com.simudap.dto.kis.oauth;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KisApiTokenResponse(
        @JsonProperty("access_token")
        String token,

        @JsonProperty("token_type")
        String tokenType,

        @JsonProperty("expires_in")
        Long expiresIn,

        @JsonProperty("access_token_token_expired")
        String tokenExpired

) {

}
