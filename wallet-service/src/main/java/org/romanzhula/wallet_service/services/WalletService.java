package org.romanzhula.wallet_service.services;

import lombok.RequiredArgsConstructor;
import org.romanzhula.wallet_service.configurations.RabbitmqConfig;
import org.romanzhula.wallet_service.models.Wallet;
import org.romanzhula.wallet_service.models.events.BalanceOperationEvent;
import org.romanzhula.wallet_service.models.events.ExpensesResponseEvent;
import org.romanzhula.wallet_service.repositories.WalletRepository;
import org.romanzhula.wallet_service.responses.CommonWalletResponse;
import org.romanzhula.wallet_service.responses.WalletBalanceResponse;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final RabbitTemplate rabbitTemplate;
    private final RabbitmqConfig rabbitmqConfig;
    private final TransactionalOperator transactionalOperator;


    public Flux<CommonWalletResponse> getAllWallets() {
        return walletRepository.findAll()
                .map(wallet -> new CommonWalletResponse(wallet.getUserId(), wallet.getBalance()))
        ;
    }

    public Mono<CommonWalletResponse> getWalletById(UUID walletId) {
        return walletRepository.findById(walletId)
                .map(wallet -> new CommonWalletResponse(wallet.getUserId(), wallet.getBalance()))
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Wallet not found with id: " + walletId)))
        ;
    }

    public Mono<WalletBalanceResponse> getBalanceByWalletId(UUID walletId) {
        return walletRepository.findById(walletId)
                .map(wallet -> new WalletBalanceResponse(wallet.getBalance()))
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Wallet not found with id: " + walletId)))
        ;
    }

    public Mono<String> replenishBalance(BalanceOperationEvent balanceOperationEvent) {
        validateBalanceReplenishRequest(balanceOperationEvent);

        return fetchWalletById(balanceOperationEvent.getUserId())
                .flatMap(wallet -> {
                    wallet.setBalance(wallet.getBalance().add(balanceOperationEvent.getAmount()));
                    return walletRepository.save(wallet);
                })
                .as(transactionalOperator::transactional)
                .doOnSuccess(wallet -> {
                    String description = String.format(
                            "Operation replenish: +%s, account balance: %s",
                            balanceOperationEvent.getAmount(),
                            wallet.getBalance()
                    );

                    sendDataToQueueWalletReplenished(balanceOperationEvent, description);
                    sendDataToQueueWalletReplenishedForExpensesService(balanceOperationEvent);
                })
                .thenReturn("Balance replenished successfully.")
        ;
    }

    public Mono<String> deductBalance(BalanceOperationEvent balanceOperationEvent) {
        validateDeductionRequest(balanceOperationEvent);

        return fetchWalletById(balanceOperationEvent.getUserId())
                .flatMap(wallet -> {
                    checkSufficientFunds(wallet, balanceOperationEvent.getAmount());
                    wallet.setBalance(wallet.getBalance().subtract(balanceOperationEvent.getAmount()));
                    return walletRepository.save(wallet);
                })
                .as(transactionalOperator::transactional)
                .doOnSuccess(wallet -> {
                    String description = String.format(
                            "Operation deduct: -%s, account balance: %s",
                            balanceOperationEvent.getAmount(),
                            wallet.getBalance()
                    );

                    sendDataToQueueWalletReplenished(balanceOperationEvent, description);
                })
                .thenReturn("Your balance successfully deducted!")
        ;
    }

    private void validateBalanceReplenishRequest(BalanceOperationEvent balanceOperationEvent) {
        if (balanceOperationEvent.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("The top-up amount must be greater than 0.");
        }
    }

    private Mono<Wallet> fetchWalletById(UUID walletId) {
        return walletRepository.findById(walletId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Wallet not found with id: " + walletId)));
    }

    private void validateDeductionRequest(BalanceOperationEvent balanceOperationEvent) {
        if (balanceOperationEvent.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("The deduction amount must be greater than 0.");
        }

        if (balanceOperationEvent.getUserId() == null) {
            throw new IllegalArgumentException("Wallet ID cannot be null or empty.");
        }
    }

    private void checkSufficientFunds(Wallet wallet, BigDecimal deductionAmount) {
        if (wallet.getBalance().compareTo(deductionAmount) < 0) {
            throw new IllegalArgumentException("Insufficient funds in the wallet.");
        }
    }

    private void sendDataToQueueWalletReplenished(BalanceOperationEvent balanceOperationEvent, String description) {
        rabbitTemplate.convertAndSend(
                rabbitmqConfig.getQueueWalletReplenished(),
                new BalanceOperationEvent(
                        balanceOperationEvent.getUserId(),
                        balanceOperationEvent.getAmount(),
                        description
                )
        );
    }

    private void sendDataToQueueWalletReplenishedForExpensesService(BalanceOperationEvent balanceOperationEvent) {
        getBalanceByWalletId(balanceOperationEvent.getUserId())
                .subscribe(remainingBalance -> {
                    ExpensesResponseEvent expensesResponseEvent = new ExpensesResponseEvent(
                            balanceOperationEvent.getUserId(),
                            "Wallet replenished",
                            balanceOperationEvent.getAmount(),
                            "Balance updated successfully",
                            remainingBalance.getBalance()
                    );

                    rabbitTemplate.convertAndSend(
                            rabbitmqConfig.getQueueWalletReplenishedForExpensesService(),
                            expensesResponseEvent
                    );
                })
        ;
    }

}
