package org.romanzhula.journal_service.repositories;

import org.romanzhula.journal_service.models.JournalEntry;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Repository
public interface JournalRepository extends ReactiveCrudRepository<JournalEntry, Long> {

    Flux<JournalEntry> findAllByUserId(UUID userId);

}
