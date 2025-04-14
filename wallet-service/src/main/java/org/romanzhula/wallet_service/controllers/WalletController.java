package org.romanzhula.wallet_service.controllers;

import lombok.RequiredArgsConstructor;
import org.romanzhula.wallet_service.models.events.BalanceOperationEvent;
import org.romanzhula.wallet_service.responses.CommonWalletResponse;
import org.romanzhula.wallet_service.responses.WalletBalanceResponse;
import org.romanzhula.wallet_service.services.WalletService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/wallets")
public class WalletController {

    private final WalletService walletService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public Mono<ResponseEntity<Flux<CommonWalletResponse>>> getAll() {
        Flux<CommonWalletResponse> wallets = walletService.getAllWallets();

        return Mono.just(ResponseEntity.ok(wallets));
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("/{wallet-id}")
    public Mono<ResponseEntity<CommonWalletResponse>> getWalletById(
            @PathVariable("wallet-id") UUID walletId
    ) {
        return walletService.getWalletById(walletId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build())
        ;
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN','ROLE_USER')")
    @GetMapping("/{wallet-id}/balance")
    public Mono<ResponseEntity<WalletBalanceResponse>> getBalanceByWalletId(
            @PathVariable("wallet-id") UUID walletId
    ) {
        return walletService.getBalanceByWalletId(walletId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build())
        ;
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @PostMapping("/up-balance")
    public Mono<ResponseEntity<String>> replenishBalance(
            @RequestBody BalanceOperationEvent balanceOperationEvent
    ) {
        return walletService.replenishBalance(balanceOperationEvent)
                .map(response -> ResponseEntity.ok(response))
                .onErrorResume(e -> Mono.just(ResponseEntity.badRequest().body(e.getMessage())))
        ;
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @PatchMapping("/deduct-balance")
    public Mono<ResponseEntity<String>> deductBalance(
            @RequestBody BalanceOperationEvent balanceOperationEvent
    ) {
        return walletService.deductBalance(balanceOperationEvent)
                .map(response -> ResponseEntity.ok(response))
                .onErrorResume(e -> Mono.just(ResponseEntity.badRequest().body(e.getMessage())))
        ;
    }

}
