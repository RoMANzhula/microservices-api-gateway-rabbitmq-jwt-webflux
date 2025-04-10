package org.romanzhula.user_service.configurations;

import com.rabbitmq.client.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.*;

@Slf4j
@Configuration
public class RabbitmqConfig {

    @Value("${spring.rabbitmq.username}")
    private String username;

    @Value("${spring.rabbitmq.password}")
    private String password;

    @Value("${spring.rabbitmq.host}")
    private String host;

    @Value("${rabbitmq.queue.name.user-created}")
    private String queueUserCreated;


    @Bean
    public Queue userCreatedQueue() {
        return new Queue(queueUserCreated, true); // durable "true" - this queue will persist after server stop
    }

    @Bean(name = "customRabbitConnectionFactory")
    public ConnectionFactory customRabbitConnectionFactory() {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.useNio();
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        connectionFactory.setHost(host);
        return connectionFactory;
    }

    @Bean
    public Sender sender(ConnectionFactory connectionFactory) {
        SenderOptions senderOptions = new SenderOptions()
                .connectionFactory(connectionFactory);
        return RabbitFlux.createSender(senderOptions);
    }

    @Bean
    public Receiver receiver(ConnectionFactory connectionFactory) {
        ReceiverOptions receiverOptions = new ReceiverOptions()
                .connectionFactory(connectionFactory);
        return RabbitFlux.createReceiver(receiverOptions);
    }

    @Bean
    public Mono<Void> declareQueue(Sender sender, Queue userCreatedQueue) {
        return sender
                .declare(QueueSpecification.queue(userCreatedQueue.getName()).durable(true))
                .doOnSuccess(response -> log.info(
                        "Queue {} is created or already exists.",
                        userCreatedQueue.getName()
                ))
                .then()
        ;
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

}
