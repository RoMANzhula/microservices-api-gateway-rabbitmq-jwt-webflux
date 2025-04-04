package org.romanzhula.user_service.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.romanzhula.microservices_common.security.implementations.UserDetailsImpl;
import org.romanzhula.microservices_common.security.jwt.CommonJWTService;
import org.romanzhula.user_service.controllers.requests.LoginRequest;
import org.romanzhula.user_service.controllers.requests.RegistrationRequest;
import org.romanzhula.user_service.controllers.responses.AuthResponse;
import org.romanzhula.user_service.models.User;
import org.romanzhula.user_service.models.enums.UserRole;
import org.romanzhula.user_service.repositories.UserRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthenticationService {

    private final PasswordEncoder passwordEncoder;
    private final TransactionalOperator transactionalOperator;
    private final UserRepository userRepository;
    private final RabbitTemplate rabbitTemplate;
    private final CommonJWTService jwtService;
    private final ReactiveAuthenticationManager authenticationManager;


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
                            var userDetails = new UserDetailsImpl(
                                    savedUser.getId().toString(),
                                    savedUser.getUsername(),
                                    savedUser.getPassword(),
                                    List.of(savedUser.getRole().name())
                            );

                            return jwtService.generateToken(userDetails)
                                    .map(jwtToken -> AuthResponse.builder()
                                            .token(jwtToken)
                                            .build()
                                    );
                        })
                        .doOnError(error -> status.setRollbackOnly())
        ).single();
    }


    public Mono<AuthResponse> login(LoginRequest loginRequest) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                );

        return authenticationManager.authenticate(authenticationToken)
                .map(authentication -> (UserDetailsImpl) authentication.getPrincipal())
                .flatMap(userDetails -> {
                    return jwtService.generateToken(userDetails)
                            .map(token -> AuthResponse.builder()
                                    .token(token)
                                    .build())
                            ;
                    }
                )
        ;
    }

}
