package org.romanzhula.expenses_service.responses;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;


@Getter
@AllArgsConstructor
public class ExpensesResponse {

    private Long id;
    private UUID userId;
    private String title;
    private BigDecimal amount;
    private String message;
    private BigDecimal remainingBalance;

}
