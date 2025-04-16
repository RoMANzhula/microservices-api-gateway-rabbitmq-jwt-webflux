package org.romanzhula.expenses_service.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("expenses")
public class Expense {

    @Id
    @Column("expense_id")
    private Long id;

    private UUID userId;

    @Column("expense_title")
    private String title;

    private BigDecimal amount;

    @Column("message_request")
    private String message;

    private BigDecimal remainingBalance;

}
