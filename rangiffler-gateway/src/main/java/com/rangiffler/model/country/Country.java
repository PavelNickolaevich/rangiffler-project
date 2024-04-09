package com.rangiffler.model.country;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Country(
        @JsonProperty("flag")
        String flag,
        @JsonProperty("code")
        String code,
        @JsonProperty("name")
        String name) {
}
