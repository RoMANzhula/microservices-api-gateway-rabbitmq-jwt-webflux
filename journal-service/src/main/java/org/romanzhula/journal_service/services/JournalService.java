package org.romanzhula.journal_service.services;

import lombok.RequiredArgsConstructor;
import org.romanzhula.journal_service.models.JournalEntry;
import org.romanzhula.journal_service.repositories.JournalRepository;
import org.romanzhula.journal_service.responses.JournalResponse;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.UUID;


@Service
@RequiredArgsConstructor
public class JournalService {

    private final JournalRepository journalRepository;


    public Flux<JournalResponse> getAllEntries() {
        return journalRepository.findAll()
                .map(this::toJournalResponse)
        ;
    }

    public Flux<JournalResponse> getAllUserJournalEntries(String userId) {
        return journalRepository.findAllByUserId(UUID.fromString(userId))
                .map(this::toJournalResponse)
        ;
    }

    private JournalResponse toJournalResponse(JournalEntry journalEntry) {
        return new JournalResponse(
                journalEntry.getId(),
                journalEntry.getUserId(),
                journalEntry.getDescription(),
                journalEntry.getCreatedAt()
        );
    }

}
