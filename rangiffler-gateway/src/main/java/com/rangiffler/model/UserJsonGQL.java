package com.rangiffler.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.rangiffler.config.RangifflerGatewayServiceConfig;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UserJsonGQL(
        @JsonProperty("avatar")
        @Size(max = RangifflerGatewayServiceConfig.ONE_MB)
        String photo,
        @JsonProperty("firstname")
        @Size(max = 30, message = "First name can`t be longer than 30 characters")
        String firstname,
        @JsonProperty("id")
        UUID id,
        @JsonProperty("surname")
        @Size(max = 50, message = "Surname can`t be longer than 50 characters")
        String surname,
        @JsonProperty("username")
        String username,
        @JsonProperty("friendStatus")
        @JsonInclude(JsonInclude.Include.NON_NULL)
        FriendStatus friendStatus,
        @JsonProperty("location")
        Country location
) {

}
