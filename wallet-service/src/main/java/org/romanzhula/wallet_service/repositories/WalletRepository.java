package org.romanzhula.wallet_service.repositories;

import org.romanzhula.wallet_service.models.Wallet;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

@Repository
public interface WalletRepository extends ReactiveCrudRepository<Wallet, UUID> {

    @Modifying
    @Query("UPDATE wallets SET balance = :balance WHERE wallet_id = :walletId")
    Mono<Void> saveWallet(@Param("walletId") UUID walletId, @Param("balance") BigDecimal balance);

}
