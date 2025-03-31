package org.romanzhula.wallet_service.models;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wallet {

    private UUID userId;

    private BigDecimal balance;

}
