package com.akalea.commons.messaging.services.rabbit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RabbitMqProducer extends RabbitMqClient {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private String routingKey;

    public RabbitMqProducer(
        String host,
        int port,
        String username,
        String password,
        String exchangeName,
        String exchangeType,
        String routingKey) {
        super(host, port, username, password, exchangeName, exchangeType);
        this.routingKey = routingKey;
    }

    public void publish(String message) {
        publish(message, this.routingKey, true);
    }

    public void publish(String message, String routingKey, boolean autoDisconnect) {
        try {
            setupExchange();
            channel.basicPublish(exchangeName, routingKey, null, message.getBytes());
        } catch (Exception e) {
            log.error("Error while publishing message ", e);
        } finally {
            if (autoDisconnect)
                this.disconnect();
        }
    }
}
