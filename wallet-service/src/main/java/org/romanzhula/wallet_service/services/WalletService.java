package org.romanzhula.wallet_service.services;

import lombok.RequiredArgsConstructor;
import org.romanzhula.microservices_common.dto.UserResponse;
import org.romanzhula.microservices_common.utils.UserServiceWebClient;
import org.romanzhula.wallet_service.configurations.RabbitmqConfig;
import org.romanzhula.wallet_service.models.Wallet;
import org.romanzhula.wallet_service.models.events.BalanceOperationEvent;
import org.romanzhula.wallet_service.models.events.ExpensesResponseEvent;
import org.romanzhula.wallet_service.repositories.WalletRepository;
import org.romanzhula.wallet_service.responses.CommonWalletResponse;
import org.romanzhula.wallet_service.responses.WalletBalanceResponse;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.userdetails.User;
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
    private final UserServiceWebClient userServiceWebClient;

    public Flux<CommonWalletResponse> getAllWallets() {
        return walletRepository.findAll()
                .map(wallet -> new CommonWalletResponse(wallet.getUserId(), wallet.getBalance()));
    }

    public Mono<CommonWalletResponse> getWalletById(UUID walletId) {
        return getCurrentUserId().flatMap(currentUserId ->
                walletRepository.findById(walletId)
                        .flatMap(wallet -> {
                            if (!wallet.getUserId().toString().equals(currentUserId)) {
                                return Mono.error(new SecurityException("Access denied to wallet with id: " + walletId));
                            }
                            return Mono.just(new CommonWalletResponse(wallet.getUserId(), wallet.getBalance()));
                        })
                        .switchIfEmpty(Mono.error(
                                new IllegalArgumentException("Wallet or User not found with id: " + walletId))
                        )
                )
        ;
    }

    public Mono<WalletBalanceResponse> getBalanceByWalletId(UUID walletId) {
        return getCurrentUserId().flatMap(currentUserId ->
                walletRepository.findById(walletId)
                        .flatMap(wallet -> {
                            if (!wallet.getUserId().toString().equals(currentUserId)) {
                                return Mono.error(new SecurityException("Access denied to wallet with id: " + walletId));
                            }
                            return Mono.just(new WalletBalanceResponse(wallet.getBalance()));
                        })
                        .switchIfEmpty(Mono.error(
                                new IllegalArgumentException("Wallet or User not found with id: " + walletId))
                        )
                )
        ;
    }

    public Mono<String> replenishBalance(BalanceOperationEvent event) {
        validateBalanceReplenishRequest(event);

        return getCurrentUserId().flatMap(currentUserId -> {
            if (!event.getUserId().equals(currentUserId)) {
                return Mono.error(new SecurityException("You are not allowed to replenish this wallet."));
            }

            return fetchWalletById(event.getUserId())
                    .flatMap(wallet -> {
                        wallet.setBalance(wallet.getBalance().add(event.getAmount()));
                        return walletRepository.save(wallet);
                    })
                    .as(transactionalOperator::transactional)
                    .doOnSuccess(wallet -> {
                        String description = String.format(
                                "Operation replenish: +%s, account balance: %s",
                                event.getAmount(), wallet.getBalance()
                        );

                        sendDataToQueueWalletReplenished(event, description);
                        sendDataToQueueWalletReplenishedForExpensesService(event);
                    })
                    .thenReturn("Balance replenished successfully.");
            }
        );
    }

    public Mono<String> deductBalance(BalanceOperationEvent event) {
        validateDeductionRequest(event);

        return getCurrentUserId().flatMap(currentUserId -> {
            if (!event.getUserId().toString().equals(currentUserId)) {
                return Mono.error(new SecurityException("You are not allowed to deduct from this wallet."));
            }

            return fetchWalletById(event.getUserId())
                    .flatMap(wallet -> {
                        checkSufficientFunds(wallet, event.getAmount());
                        wallet.setBalance(wallet.getBalance().subtract(event.getAmount()));
                        return walletRepository.save(wallet);
                    })
                    .as(transactionalOperator::transactional)
                    .doOnSuccess(wallet -> {
                        String description = String.format(
                                "Operation deduct: -%s, account balance: %s",
                                event.getAmount(), wallet.getBalance()
                        );

                        sendDataToQueueWalletReplenished(event, description);
                    })
                    .thenReturn("Your balance successfully deducted!");
            }
        );
    }

    // fetching current user from context
    private Mono<String> getCurrentUserId() {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> {
                    Object principal = ctx.getAuthentication().getPrincipal();
                    if (principal instanceof User user) {
                        return user.getUsername();
                    } else {
                        throw new IllegalStateException("Expected User but got: " + principal.getClass());
                    }
                })
                .flatMap(userServiceWebClient::getUserByUsername)
                .map(UserResponse::getId)
        ;
    }


    private void validateBalanceReplenishRequest(BalanceOperationEvent event) {
        if (event.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("The top-up amount must be greater than 0.");
        }
    }

    private Mono<Wallet> fetchWalletById(UUID walletId) {
        return walletRepository.findById(walletId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Wallet not found with id: " + walletId)))
        ;
    }

    private void validateDeductionRequest(BalanceOperationEvent event) {
        if (event.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("The deduction amount must be greater than 0.");
        }

        if (event.getUserId() == null) {
            throw new IllegalArgumentException("Wallet ID cannot be null or empty.");
        }
    }

    private void checkSufficientFunds(Wallet wallet, BigDecimal deductionAmount) {
        if (wallet.getBalance().compareTo(deductionAmount) < 0) {
            throw new IllegalArgumentException("Insufficient funds in the wallet.");
        }
    }

    private void sendDataToQueueWalletReplenished(BalanceOperationEvent event, String description) {
        rabbitTemplate.convertAndSend(
                rabbitmqConfig.getQueueWalletReplenished(),
                new BalanceOperationEvent(event.getUserId(), event.getAmount(), description)
        );
    }

    private void sendDataToQueueWalletReplenishedForExpensesService(BalanceOperationEvent event) {
        getBalanceByWalletId(event.getUserId())
                .subscribe(balance -> {
                    ExpensesResponseEvent responseEvent = new ExpensesResponseEvent(
                            event.getUserId(),
                            "Wallet replenished",
                            event.getAmount(),
                            "Balance updated successfully",
                            balance.getBalance()
                    );

                    rabbitTemplate.convertAndSend(
                            rabbitmqConfig.getQueueWalletReplenishedForExpensesService(),
                            responseEvent
                    );
                })
        ;
    }

}
