package com.rangiffler.service.cors.api;

import com.rangiffler.ex.NoRestResponseException;
import com.rangiffler.model.user.UserJsonGQL;
import jakarta.annotation.Nonnull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Optional;

@Component
public class RestUserDataClient {

    private final WebClient webClient;
    private final String rangifflerUserdataBaseUri;


    @Autowired
    public RestUserDataClient(WebClient webClient,
                              @Value("${rangiffler-userdata.base-uri}") String rangifflerUserdataBaseUri) {
        this.webClient = webClient;
        this.rangifflerUserdataBaseUri = rangifflerUserdataBaseUri;
    }


    public @Nonnull
    UserJsonGQL currentUser(@Nonnull String username) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("username", username);
        URI uri = UriComponentsBuilder.fromHttpUrl(rangifflerUserdataBaseUri + "/currentUser").queryParams(params).build().toUri();

        return Optional.ofNullable(
                webClient.get()
                        .uri(uri)
                        .retrieve()
                        .bodyToMono(UserJsonGQL.class)
                        .block()
        ).orElseThrow(() -> new NoRestResponseException("No REST UserJson response is given [/currentUser Route]"));
    }

    public @Nonnull
    UserJsonGQL updateUserInfo(@Nonnull UserJsonGQL user) {
        return Optional.ofNullable(
                webClient.post()
                        .uri(rangifflerUserdataBaseUri + "/updateUserInfo")
                        .body(Mono.just(user), UserJsonGQL.class)
                        .retrieve()
                        .bodyToMono(UserJsonGQL.class)
                        .block()
        ).orElseThrow(() -> new NoRestResponseException("No REST UserJson response is given [/updateUserInfo Route]"));
    }
}
