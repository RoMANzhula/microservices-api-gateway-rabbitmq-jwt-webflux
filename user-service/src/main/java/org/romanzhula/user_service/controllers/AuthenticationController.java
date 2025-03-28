package org.romanzhula.user_service.controllers;

import lombok.RequiredArgsConstructor;
import org.romanzhula.user_service.controllers.requests.LoginRequest;
import org.romanzhula.user_service.controllers.requests.RegistrationRequest;
import org.romanzhula.user_service.controllers.responses.AuthResponse;
import org.romanzhula.user_service.services.AuthenticationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;


    @PostMapping("/register")
    public Mono<ResponseEntity<AuthResponse>> registration(
            @RequestBody Mono<RegistrationRequest> registrationRequest
    ) {
        return registrationRequest
                .flatMap(authenticationService::userRegistration)
                .map(ResponseEntity::ok)
        ;
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<AuthResponse>> login(
            @RequestBody Mono<LoginRequest> loginRequest
    ) {
        return loginRequest
                .flatMap(authenticationService::login)
                .map(ResponseEntity::ok)
        ;
    }

}
