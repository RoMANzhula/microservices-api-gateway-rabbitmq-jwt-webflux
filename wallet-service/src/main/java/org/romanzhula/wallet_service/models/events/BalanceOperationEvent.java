package org.romanzhula.wallet_service.models.events;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class BalanceOperationEvent {

    private UUID userId;
    private BigDecimal amount;
    private String description;

}
