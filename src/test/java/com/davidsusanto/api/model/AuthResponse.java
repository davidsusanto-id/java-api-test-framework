package com.davidsusanto.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * The API returns either a token or a rejection reason, never both.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AuthResponse(String token, String reason) {
}
