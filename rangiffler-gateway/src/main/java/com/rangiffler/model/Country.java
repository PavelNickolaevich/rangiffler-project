package com.rangiffler.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Country(

        @JsonProperty("flag")
        String flag,
        @JsonProperty("code")
        String code,
        @JsonProperty("name")
        String name
) {
}
