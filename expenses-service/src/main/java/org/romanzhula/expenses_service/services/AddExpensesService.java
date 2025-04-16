package org.romanzhula.expenses_service.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Delivery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.romanzhula.expenses_service.configurations.RabbitmqConfig;
import org.romanzhula.expenses_service.models.Expense;
import org.romanzhula.expenses_service.models.events.ExpensesRequestEvent;
import org.romanzhula.expenses_service.repositories.ExpensesRepository;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.Receiver;


@Service
@RequiredArgsConstructor
@Slf4j
public class AddExpensesService implements ApplicationListener<ContextRefreshedEvent> {

    private final Receiver receiver;
    private final RabbitmqConfig rabbitmqConfig;
    private final ExpensesRepository expensesRepository;
    private final ObjectMapper objectMapper;
    private final TransactionalOperator transactionalOperator;


    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        startListening();
    }

    public void startListening() {
        consumeMessages();
    }

    private void consumeMessages() {
        receiver.consumeAutoAck(rabbitmqConfig.getQueueWalletReplenishedForExpensesService()) // queues listening
                .flatMap(message -> {
                    ExpensesRequestEvent expensesRequestEvent = parseMessage(message);
                    return handleExpensesSaveFromWallet(expensesRequestEvent);
                })
                .doOnError(error -> log.error("Error processing message: {}", error.getMessage()))
                .subscribe()
        ;
    }

    private ExpensesRequestEvent parseMessage(Delivery message) {
        try {
            String body = new String(message.getBody());
            return objectMapper.readValue(body, ExpensesRequestEvent.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse message", e);
        }
    }

    private Mono<Void> handleExpensesSaveFromWallet(ExpensesRequestEvent expensesRequestEvent) {
        Expense expense = new Expense();
        expense.setUserId(expensesRequestEvent.getUserId());
        expense.setTitle(expensesRequestEvent.getTitle());
        expense.setAmount(expensesRequestEvent.getAmount());
        expense.setMessage(expensesRequestEvent.getMessage());
        expense.setRemainingBalance(expensesRequestEvent.getRemainingBalance());

        log.info("Creating expense for wallet: {}, amount: {}",
                expensesRequestEvent.getUserId(),
                expensesRequestEvent.getAmount()
        );

        return transactionalOperator.transactional(
                expensesRepository.save(expense)
                        .doOnSuccess(savedExpense ->
                                log.info("Expense created for wallet: {}", expensesRequestEvent.getUserId())
                        )
                        .then()
                )
        ;
    }

}
