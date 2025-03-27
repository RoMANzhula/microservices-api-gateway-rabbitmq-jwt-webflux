package org.romanzhula.user_service.services;

import lombok.RequiredArgsConstructor;
import org.romanzhula.user_service.controllers.responses.UserFeignResponse;
import org.romanzhula.user_service.controllers.responses.UserResponse;
import org.romanzhula.user_service.repositories.UserRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public Flux<UserResponse> getAll(int page, int size) {
        return userRepository.findAllBy(page, size)
                .map(user -> new UserResponse(
                        user.getId(),
                        user.getUsername(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getEmail(),
                        user.getPhone()
                ))
        ;
    }


    public Mono<UserResponse> getUserById(String userId) {
        return userRepository.findById(UUID.fromString(userId))
                .map(user -> new UserResponse(
                        user.getId(),
                        user.getUsername(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getEmail(),
                        user.getPhone()
                ))
                .switchIfEmpty(Mono.error(new RuntimeException("User not found with id: " + userId)))
        ;
    }


    public Mono<UserFeignResponse> getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(user -> new UserFeignResponse(
                        user.getId().toString(),
                        user.getUsername(),
                        user.getPassword(),
                        Collections.singleton(user.getRole().toString())
                ))
                .switchIfEmpty(Mono.error(new RuntimeException("User not found with username: " + username)))
        ;
    }

}
