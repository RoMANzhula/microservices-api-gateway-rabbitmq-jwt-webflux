package org.romanzhula.journal_service.responses;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;


@Getter
@AllArgsConstructor
public class JournalResponse {

    private Long id;
    private UUID userId;
    private String description;
    private LocalDateTime createdAt;

}
