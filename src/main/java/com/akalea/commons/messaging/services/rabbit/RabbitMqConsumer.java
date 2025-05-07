package com.akalea.commons.messaging.services.rabbit;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

public class RabbitMqConsumer extends RabbitMqClient {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private String   queueName;
    private boolean  durable;
    private boolean  exclusive;
    private boolean  autoDelete;
    private String   routingKey;
    private Consumer consumer;

    public RabbitMqConsumer(
        String host,
        int port,
        String username,
        String password,
        String exchangeName,
        String exchangeType,
        String queueName,
        boolean durable,
        boolean exclusive,
        boolean autoDelete,
        String routingKey) {
        super(host, port, username, password, exchangeName, exchangeType);
        this.routingKey = routingKey;
        this.durable = durable;
        this.exclusive = exclusive;
        this.autoDelete = autoDelete;

        this.queueName = queueName;
        if (this.exclusive && !this.durable)
            this.queueName =
                String.format(
                    "%s-%s",
                    queueName,
                    UUID.randomUUID().toString().substring(0, 10));

    }

    public RabbitMqConsumer(RabbitMqConsumerConfiguration configuration) {
        this(
            configuration.getHost(),
            configuration.getPort(),
            configuration.getUsername(),
            configuration.getPassword(),
            configuration.getExchangeName(),
            configuration.getExchangeType(),
            configuration.getQueueName(),
            configuration.isDurable(),
            configuration.isExclusive(),
            configuration.isAutoDelete(),
            configuration.getRoutingKey());
        this.prefetchCount = configuration.getPrefetchCount();
    }

    private void setupQueue() throws IOException, TimeoutException {
        try {
            if (channel == null)
                setupExchange();
            channel.queueDeclare(
                queueName,
                durable,
                exclusive,
                autoDelete,
                new HashMap<String, Object>());
            channel.queueBind(queueName, exchangeName, routingKey);
        } catch (Exception e) {
            log.error("Error setting up queue", e);
        }
    }

    public void startConsuming(Function<String, Boolean> messageCallback) {
        try {
            setupQueue();
            consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(
                    String consumerTag,
                    Envelope envelope,
                    AMQP.BasicProperties properties,
                    byte[] body)
                    throws IOException {
                    try {
                        String message = new String(body, "UTF-8");
                        messageCallback.apply(message);
                    } catch (Exception e) {
                        log.error("Error while consuming message ", e);
                    }
                }
            };
            channel.basicConsume(queueName, true, consumer);
        } catch (Exception e) {
            log.error("Error while consuming messages ", e);
        }
    }
}
