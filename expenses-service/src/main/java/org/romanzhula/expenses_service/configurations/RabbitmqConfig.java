package org.romanzhula.expenses_service.configurations;

import lombok.Getter;
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

    @Value("${rabbitmq.queue.name.expense-added}")
    private String queueWalletReplenishedForExpensesService;

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

}
