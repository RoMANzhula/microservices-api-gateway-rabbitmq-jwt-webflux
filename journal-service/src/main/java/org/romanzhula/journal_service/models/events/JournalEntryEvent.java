package org.romanzhula.journal_service.models.events;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;


@Data
public class JournalEntryEvent {

    private UUID userId;
    private BigDecimal amount;
    private String description;

}
