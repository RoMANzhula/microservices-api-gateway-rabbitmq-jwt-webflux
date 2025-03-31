package org.romanzhula.wallet_service.models.events;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
public class ExpensesResponseEvent {

    private UUID userId;
    private String title;
    private BigDecimal amount;
    private String message;
    private BigDecimal remainingBalance;

}
