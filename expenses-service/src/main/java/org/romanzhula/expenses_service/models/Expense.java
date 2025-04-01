package org.romanzhula.expenses_service.models;

import lombok.*;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("expenses")
public class Expense {

    private Long id;

    private UUID userId;
    private String title;
    private BigDecimal amount;
    private String message;
    private BigDecimal remainingBalance;

}
