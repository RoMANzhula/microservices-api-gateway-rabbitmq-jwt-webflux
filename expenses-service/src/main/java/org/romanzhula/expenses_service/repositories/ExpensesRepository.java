package org.romanzhula.expenses_service.repositories;

import org.romanzhula.expenses_service.models.Expense;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Repository
public interface ExpensesRepository extends ReactiveCrudRepository<Expense, Long> {

    Flux<Expense> findAllByUserId(UUID userId);

}
