package org.romanzhula.expenses_service.services;

import lombok.RequiredArgsConstructor;
import org.romanzhula.expenses_service.models.Expense;
import org.romanzhula.expenses_service.repositories.ExpensesRepository;
import org.romanzhula.expenses_service.responses.ExpensesResponse;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.UUID;


@Service
@RequiredArgsConstructor
public class ExpensesService {

    private final ExpensesRepository expensesRepository;


    public Flux<ExpensesResponse> getAllExpensesByUserId(String userId) {
        return expensesRepository.findAllByUserId(UUID.fromString(userId))
                .map(this::toExpensesResponse)
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

}