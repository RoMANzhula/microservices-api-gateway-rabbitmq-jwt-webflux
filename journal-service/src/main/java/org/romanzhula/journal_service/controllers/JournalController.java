package org.romanzhula.journal_service.controllers;

import lombok.RequiredArgsConstructor;
import org.romanzhula.journal_service.responses.JournalResponse;
import org.romanzhula.journal_service.services.JournalService;
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
@RequestMapping("/api/v1/journal")
public class JournalController {

    private final JournalService journalService;

    @PreAuthorize("hasRole('ADMIN')") // same with "hasAuthority('ROLE_ADMIN')"
    @GetMapping("/all")
    public Mono<ResponseEntity<Flux<JournalResponse>>> getAllEntries() {
        Flux<JournalResponse> entries = journalService.getAllEntries();
        return Mono.just(ResponseEntity.ok(entries));
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/{user-id}")
    public Mono<ResponseEntity<Flux<JournalResponse>>> getAllUserJournalEntries(
            @PathVariable("user-id") String userId
    ) {
        Flux<JournalResponse> entries = journalService.getAllUserJournalEntries(userId);
        return Mono.just(ResponseEntity.ok(entries));
    }

}
