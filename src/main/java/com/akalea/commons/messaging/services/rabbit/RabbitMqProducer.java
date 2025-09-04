package com.akalea.commons.messaging.services.rabbit;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.Channel;

public class RabbitMqProducer extends RabbitMqClient {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private String routingKey;

    private long confirmPublishDelayMsec;

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

    public RabbitMqProducer(RabbitMqConsumerConfiguration configuration) {
        this(
            configuration.getHost(),
            configuration.getPort(),
            configuration.getUsername(),
            configuration.getPassword(),
            configuration.getExchangeName(),
            configuration.getExchangeType(),
            configuration.getRoutingKey());
        this.confirmPublish = configuration.isConfirmPublish();
        this.confirmPublishDelayMsec = configuration.getConfirmPublishDelayMsec();

    }

    public void publish(String message) {
        publish(message, this.routingKey, true);
    }

    public void publish(String message, String routingKey, boolean autoDisconnect) {
        try {
            connect();
            for (int i = 0; i < 3; i++) {
                boolean throwError = i == 3;
                if (doPublish(message, routingKey, throwError))
                    return;
            }
        } catch (Exception e) {
            log.error("Error while publishing message ", e);
            throw new RuntimeException(e);
        } finally {
            if (autoDisconnect)
                this.disconnect();
        }
    }

    private boolean doPublish(String message, String routingKey, boolean throwError) {
        try (Channel channel = createChannel()) {
            channel.basicPublish(exchangeName, routingKey, null, message.getBytes());
            if (confirmPublish && this.confirmPublishDelayMsec > 0) {
                channel.waitForConfirmsOrDie(confirmPublishDelayMsec);
            }
            return true;
        } catch (Exception e) {
            log.error("Error publishing msg", e);
            if (throwError)
                throw new RuntimeException(e);
            return false;
        }
    }
}
