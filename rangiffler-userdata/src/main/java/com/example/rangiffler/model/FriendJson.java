package com.example.rangiffler.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FriendJson(
        @JsonProperty("username")
         String username

) {
}
