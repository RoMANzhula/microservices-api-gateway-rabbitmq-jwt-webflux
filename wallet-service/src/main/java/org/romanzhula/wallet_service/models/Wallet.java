package org.romanzhula.wallet_service.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("wallets")
public class Wallet {

    @Id
    @Column("wallet_id")
    private UUID userId;

    private BigDecimal balance;

}
