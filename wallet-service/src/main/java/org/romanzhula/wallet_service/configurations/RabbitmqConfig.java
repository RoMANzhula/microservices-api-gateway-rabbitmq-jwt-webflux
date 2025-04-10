package org.romanzhula.wallet_service.configurations;

import com.rabbitmq.client.ConnectionFactory;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.*;


@Configuration
@Slf4j
@Getter
public class RabbitmqConfig {

    @Value("${spring.rabbitmq.username}")
    private String username;

    @Value("${spring.rabbitmq.password}")
    private String password;

    @Value("${spring.rabbitmq.host}")
    private String host;

    @Value("${rabbitmq.queue.name.user-created}")
    private String queueUserCreated;

    @Value("${rabbitmq.queue.name.wallet-updated}")
    private String queueWalletReplenished;

    @Value("${rabbitmq.queue.name.expense-added}")
    private String queueWalletReplenishedForExpensesService;


    @Bean
    public Queue walletReplenishedQueue() {
        return new Queue(queueWalletReplenished, true); // durable "true" - this queue will persist after server stop
    }

    @Bean
    public Queue walletReplenishedForExpensesServiceQueue() {
        return new Queue(queueWalletReplenishedForExpensesService, true);
    }

    @Bean(name = "customRabbitConnectionFactory")
    public ConnectionFactory customRabbitConnectionFactory() {
        ConnectionFactory connectionFactory = new ConnectionFactory();

        connectionFactory.useNio(); // using NIO to improve performance
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        connectionFactory.setHost(host);

        return connectionFactory;
    }

    @Bean
    public Sender sender(ConnectionFactory connectionFactory) {
        SenderOptions senderOptions = new SenderOptions()
                .connectionFactory(connectionFactory)
        ;

        return RabbitFlux.createSender(senderOptions);
    }

    @Bean
    public Receiver receiver(ConnectionFactory connectionFactory) {
        ReceiverOptions receiverOptions = new ReceiverOptions()
                .connectionFactory(connectionFactory)
        ;

        return RabbitFlux.createReceiver(receiverOptions);
    }

    @Bean
    public Mono<Void> declareQueues(Sender sender) {
        return sender.declare(QueueSpecification.queue(queueWalletReplenished).durable(true))
                .doOnSuccess(response ->
                        log.info("Queue {} is created or already exists.", queueWalletReplenished)
                )
                .then(sender.declare(QueueSpecification.queue(queueWalletReplenishedForExpensesService).durable(true)))
                .doOnSuccess(response ->
                        log.info("Queue {} is created or already exists.", queueWalletReplenishedForExpensesService)
                )
                .then()
        ;
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

}
