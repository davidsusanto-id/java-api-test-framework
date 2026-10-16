package com.davidsusanto.api.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The API returns either a token or a rejection reason, never both.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AuthResponse(String token, String reason) {
    @JsonCreator
    public AuthResponse(
            @JsonProperty("token") String token,
            @JsonProperty("reason") String reason) {

        this.token = token;
        this.reason = reason;

    }
}
