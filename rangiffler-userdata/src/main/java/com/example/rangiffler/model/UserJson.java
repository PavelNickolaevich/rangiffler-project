package com.example.rangiffler.model;

import com.example.rangiffler.data.UserEntity;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public record UserJson(
        @JsonProperty("id")
        UUID id,
        @JsonProperty("username")
        String username,
        @JsonProperty("firstname")
        String firstname,
        @JsonProperty("surname")
        String surname,
        @JsonProperty("avatar")
        String avatar,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        @JsonProperty("friendStatus")
        FriendStatus status,
        @JsonProperty("country_id")
        CountryJson county
) {

    public static @Nonnull UserJson fromEntity(@Nonnull UserEntity entity, @Nullable FriendStatus friendStatus) {
        return new UserJson(
                entity.getId(),
                entity.getUsername(),
                entity.getFirstname(),
                entity.getLastName(),
                entity.getAvatar() != null && entity.getAvatar().length > 0 ? new String(entity.getAvatar(), StandardCharsets.UTF_8) : null,
                friendStatus

        );
    }

    public static @Nonnull UserJson fromEntity(@Nonnull UserEntity entity) {
        return fromEntity(entity, null);
    }
}
