package org.romanzhula.journal_service.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Delivery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.romanzhula.journal_service.configurations.RabbitmqConfig;
import org.romanzhula.journal_service.models.JournalEntry;
import org.romanzhula.journal_service.models.events.JournalEntryEvent;
import org.romanzhula.journal_service.repositories.JournalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.Receiver;


@Service
@RequiredArgsConstructor
@Slf4j
public class AddNewEntriesToJournalService {

    private final Receiver receiver;
    private final RabbitmqConfig rabbitmqConfig;
    private final JournalRepository journalRepository;
    private final ObjectMapper objectMapper;
    private final TransactionalOperator transactionalOperator;


    public void startListening() {
        consumeMessages();
    }

    private void consumeMessages() {
        receiver.consumeAutoAck(rabbitmqConfig.getQueueWalletBalanceUpdated()) // queues listening
                .flatMap(message -> {
                    JournalEntryEvent journalEntryEvent = parseMessage(message);
                    return handleJournalEntryFromWallet(journalEntryEvent);
                })
                .doOnError(error -> log.error("Error processing message: {}", error.getMessage()))
                .subscribe()
        ;
    }

    private JournalEntryEvent parseMessage(Delivery message) {
        try {
            String body = new String(message.getBody());
            return objectMapper.readValue(body, JournalEntryEvent.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse message", e);
        }
    }

    private Mono<Void> handleJournalEntryFromWallet(JournalEntryEvent journalEntryEvent) {
        JournalEntry journalEntry = new JournalEntry();
        journalEntry.setUserId(journalEntryEvent.getUserId());
        journalEntry.setDescription(journalEntryEvent.getDescription());

        log.info("Creating journal entry for wallet: {}, amount difference: {}",
                journalEntryEvent.getUserId(),
                journalEntryEvent.getAmount()
        );

        return transactionalOperator.transactional(
                journalRepository.save(journalEntry)
                        .doOnSuccess(savedEntry ->
                                log.info("Journal entry created for wallet: {}", journalEntryEvent.getUserId())
                        )
                        .then()
                )
        ;
    }

}
