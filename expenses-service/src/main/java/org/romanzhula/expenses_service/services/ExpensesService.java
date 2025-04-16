package org.romanzhula.expenses_service.services;

import lombok.RequiredArgsConstructor;
import org.romanzhula.expenses_service.models.Expense;
import org.romanzhula.expenses_service.repositories.ExpensesRepository;
import org.romanzhula.expenses_service.responses.ExpensesResponse;
import org.romanzhula.microservices_common.utils.UserServiceWebClient;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.Set;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class ExpensesService {

    private final ExpensesRepository expensesRepository;
    private final UserServiceWebClient userServiceWebClient;


    public Flux<ExpensesResponse> getAllExpensesByUserId(String userId) {
        return getCurrentUserIdAndRoles()
                .flatMapMany(pair -> {
                    String currentUserId = pair.getT1();
                    Set<String> roles = pair.getT2();

                    boolean isAdmin = roles.contains("ADMIN");

                    if (!currentUserId.equals(userId) && !isAdmin) {
                        return Flux.error(new SecurityException("Access denied to this user's expenses."));
                    }

                    return expensesRepository.findAllByUserId(UUID.fromString(userId))
                            .map(this::toExpensesResponse)
                    ;
                })
        ;
    }

    private ExpensesResponse toExpensesResponse(Expense expense) {
        return new ExpensesResponse(
                expense.getId(),
                expense.getUserId(),
                expense.getTitle(),
                expense.getAmount(),
                expense.getMessage(),
                expense.getRemainingBalance()
        );
    }

    private Mono<Tuple2<String, Set<String>>> getCurrentUserIdAndRoles() {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> {
                    Object principal = ctx.getAuthentication().getPrincipal();
                    if (principal instanceof org.springframework.security.core.userdetails.User user) {
                        return user.getUsername();
                    } else {
                        throw new IllegalStateException("Expected User but got: " + principal.getClass());
                    }
                })
                .flatMap(userServiceWebClient::getUserByUsername)
                .map(userResponse -> {
                    String id = userResponse.getId();
                    Set<String> roles = userResponse.getRoles();

                    return Tuples.of(id, roles);
                })
        ;
    }

}