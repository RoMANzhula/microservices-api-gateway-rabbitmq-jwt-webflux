package org.romanzhula.microservices_common.utils;

import org.romanzhula.microservices_common.dto.UserResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class UserServiceWebClient {

    private final WebClient webClient;


    public UserServiceWebClient(@Value("${user-service.url}") String userServiceUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(userServiceUrl + "/api/v1/users")
                .build()
        ;
    }

    public Mono<UserResponse> getUserByUsername(String username) {
        return webClient.get()
                .uri("/by-username?username={username}", username)
                .retrieve()
                .bodyToMono(UserResponse.class)
        ;
    }

}
