package org.romanzhula.expenses_service.controllers;

import lombok.RequiredArgsConstructor;
import org.romanzhula.expenses_service.responses.ExpensesResponse;
import org.romanzhula.expenses_service.services.ExpensesService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/expenses")
public class ExpensesController {

    private final ExpensesService expensesService;


    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/{user-id}")
    public Mono<ResponseEntity<Flux<ExpensesResponse>>> getAllExpensesByUserId(
            @PathVariable("user-id") String userId
    ) {
        Flux<ExpensesResponse> expenses = expensesService.getAllExpensesByUserId(userId);

        return Mono.just(ResponseEntity.ok(expenses));
    }

}
