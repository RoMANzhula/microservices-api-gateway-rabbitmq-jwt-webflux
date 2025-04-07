package org.romanzhula.user_service.controllers;

import lombok.RequiredArgsConstructor;
import org.romanzhula.user_service.controllers.responses.UserDataResponse;
import org.romanzhula.user_service.controllers.responses.UserResponse;
import org.romanzhula.user_service.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;


    @PreAuthorize("hasAuthority('ROLE_ADMIN')") //equals "hasRole('ADMIN')"
    @GetMapping("/all")
    public Flux<UserResponse> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return userService.getAll(page, size);
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("/{user-id}")
    public Mono<ResponseEntity<UserResponse>> getUserById(
            @PathVariable("user-id") String userId
    ) {
        return userService.getUserById(userId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build())
        ;
    }

    @GetMapping("/by-username")
    public Mono<ResponseEntity<UserDataResponse>> getUserByUsername(
            @RequestParam("username") String username
    ) {
        return userService.getUserByUsername(username)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build())
        ;
    }

}
