package org.romanzhula.user_service.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.romanzhula.microservices_common.security.jwt.CommonJWTService;
import org.romanzhula.microservices_common.security.jwt.interfaces.TokenProvider;
import org.romanzhula.user_service.controllers.requests.LoginRequest;
import org.romanzhula.user_service.controllers.requests.RegistrationRequest;
import org.romanzhula.user_service.controllers.responses.AuthResponse;
import org.romanzhula.user_service.models.User;
import org.romanzhula.user_service.models.enums.UserRole;
import org.romanzhula.user_service.repositories.UserRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;


@Service
@Slf4j
@RequiredArgsConstructor
public class AuthenticationService {

    private final PasswordEncoder passwordEncoder;
    private final TransactionalOperator transactionalOperator;
    private final UserRepository userRepository;
    private final RabbitTemplate rabbitTemplate;
    private final CommonJWTService jwtService;
    private final ReactiveUserDetailsService userDetailsService;
    private final TokenProvider tokenProvider;


    public Mono<AuthResponse> userRegistration(RegistrationRequest registrationRequest) {
        var newUser = User.builder()
                .username(registrationRequest.getUsername())
                .firstName(registrationRequest.getFirstName())
                .lastName(registrationRequest.getLastName())
                .email(registrationRequest.getEmail())
                .phone(registrationRequest.getPhone())
                .password(passwordEncoder.encode(registrationRequest.getPassword()))
                .role(UserRole.USER)
                .build()
        ;

        return transactionalOperator.execute(status ->
                userRepository.save(newUser)
                        .doOnSuccess(user -> rabbitTemplate.convertAndSend("user-created-queue", user))
                        .flatMap(savedUser -> {
                            User userDetails = User.builder()
                                    .username(savedUser.getUsername())
                                    .password(savedUser.getPassword())
                                    .role(UserRole.USER)
                                    .build()
                            ;

                            String jwtToken = jwtService.generateToken((UserDetails) userDetails);

                            return Mono.just(AuthResponse.builder()
                                    .token(jwtToken)
                                    .build())
                            ;
                        })
                        .doOnError(error -> status.setRollbackOnly())
                ).single()
        ;
    }


    public Mono<AuthResponse> login(LoginRequest loginRequest) {
        return userDetailsService.findByUsername(loginRequest.getUsername())
                .filter(u -> passwordEncoder.matches(loginRequest.getPassword(), u.getPassword()))
                .map(tokenProvider::generateToken)
                .map(AuthResponse::new)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED)))
        ;
    }

}
