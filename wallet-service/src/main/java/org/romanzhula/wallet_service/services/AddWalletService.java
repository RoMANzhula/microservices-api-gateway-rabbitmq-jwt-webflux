package org.romanzhula.wallet_service.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Delivery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.romanzhula.wallet_service.configurations.RabbitmqConfig;
import org.romanzhula.wallet_service.models.User;
import org.romanzhula.wallet_service.models.Wallet;
import org.romanzhula.wallet_service.repositories.WalletRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.Receiver;

import java.math.BigDecimal;

@Service
@Slf4j
@RequiredArgsConstructor
public class AddWalletService {

    private final Receiver receiver;
    private final RabbitmqConfig rabbitmqConfig;
    private final WalletRepository walletRepository;
    private final ObjectMapper objectMapper;


    public void startListening() {
        consumeMessages();
    }

    private void consumeMessages() {
        receiver
                .consumeAutoAck(rabbitmqConfig.getQueueUserCreated()) // queue listening
                .flatMap(message -> {
                    User user = parseMessage(message);
                    return handleUserCreated(user);
                })
                .doOnError(error -> log.error("Error processing message: {}", error.getMessage()))
                .subscribe()
        ;
    }

    private User parseMessage(Delivery message) {
        try {
            String body = new String(message.getBody());
            return objectMapper.readValue(body, User.class); // to User
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse message", e);
        }
    }

    private Mono<Void> handleUserCreated(User user) {
        Wallet wallet = new Wallet();
        wallet.setUserId(user.getId());
        wallet.setBalance(BigDecimal.valueOf(0.0));

        log.info("Creating wallet for user: {}", user.getUsername());

        return walletRepository.save(wallet)
                .doOnSuccess(savedWallet -> log.info("Wallet created for user: {}", user.getUsername()))
                .then()
        ;
    }

}