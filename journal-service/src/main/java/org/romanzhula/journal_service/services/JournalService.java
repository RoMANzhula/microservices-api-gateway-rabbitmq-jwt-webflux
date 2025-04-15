package org.romanzhula.journal_service.services;

import lombok.RequiredArgsConstructor;
import org.romanzhula.journal_service.models.JournalEntry;
import org.romanzhula.journal_service.repositories.JournalRepository;
import org.romanzhula.journal_service.responses.JournalResponse;
import org.romanzhula.microservices_common.dto.UserResponse;
import org.romanzhula.microservices_common.utils.UserServiceWebClient;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;


@Service
@RequiredArgsConstructor
public class JournalService {

    private final JournalRepository journalRepository;
    private final UserServiceWebClient userServiceWebClient;


    public Flux<JournalResponse> getAllEntries() {
        return journalRepository.findAll()
                .map(this::toJournalResponse)
        ;
    }

    public Flux<JournalResponse> getAllUserJournalEntries(String userId) {
        return getCurrentUserId().flatMapMany(currentUserId -> {
            if (!currentUserId.equals(userId)) {
                return Flux.error(new SecurityException("Access denied to journal entries of user with id: " + userId));
            }

            return journalRepository.findAllByUserId(UUID.fromString(userId)).map(this::toJournalResponse);
        });
    }

    private JournalResponse toJournalResponse(JournalEntry journalEntry) {
        return new JournalResponse(
                journalEntry.getId(),
                journalEntry.getUserId(),
                journalEntry.getDescription(),
                journalEntry.getCreatedAt()
        );
    }

    private Mono<String> getCurrentUserId() {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> {
                    Object principal = ctx.getAuthentication().getPrincipal();
                    if (principal instanceof User user) {
                        return user.getUsername();
                    } else {
                        throw new IllegalStateException("Expected User but got: " + principal.getClass());
                    }
                })
                .flatMap(userServiceWebClient::getUserByUsername)
                .map(UserResponse::getId)
        ;
    }

}
