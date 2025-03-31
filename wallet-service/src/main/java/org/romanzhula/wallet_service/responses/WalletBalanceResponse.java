package org.romanzhula.wallet_service.responses;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class WalletBalanceResponse {

    private BigDecimal balance;

}
