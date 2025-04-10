package org.romanzhula.journal_service.configurations;

import lombok.Getter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.rabbitmq.RabbitFlux;
import reactor.rabbitmq.Receiver;
import reactor.rabbitmq.ReceiverOptions;
import com.rabbitmq.client.ConnectionFactory;

@Getter
@Configuration
public class RabbitmqConfig {

    @Value("${rabbitmq.queue.name.wallet-updated}")
    private String queueWalletBalanceUpdated;

    @Value("${spring.rabbitmq.username}")
    private String username;

    @Value("${spring.rabbitmq.password}")
    private String password;

    @Value("${spring.rabbitmq.host}")
    private String host;


    @Bean
    public Receiver receiver(ConnectionFactory connectionFactory) {
        ReceiverOptions receiverOptions = new ReceiverOptions()
                .connectionFactory(connectionFactory)
        ;

        return RabbitFlux.createReceiver(receiverOptions);
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
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

}
