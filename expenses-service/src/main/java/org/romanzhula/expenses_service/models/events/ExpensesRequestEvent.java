package org.romanzhula.expenses_service.models.events;

import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
public class ExpensesRequestEvent {

    private UUID userId;
    private String title;
    private BigDecimal amount;
    private String message;
    private BigDecimal remainingBalance;

}
