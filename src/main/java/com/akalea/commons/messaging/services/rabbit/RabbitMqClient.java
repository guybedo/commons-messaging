package com.akalea.commons.messaging.services.rabbit;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;

public class RabbitMqClient {

    private final static Logger logger = LoggerFactory.getLogger(RabbitMqClient.class);

    protected String     host;
    protected int        port;
    protected String     username;
    protected String     password;
    protected String     exchangeName;
    protected String     exchangeType;
    protected Connection connection;
    protected Channel    channel;

    public RabbitMqClient(
        String host,
        int port,
        String username,
        String password,
        String exchangeName,
        String exchangeType) {
        super();
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.exchangeName = exchangeName;
        this.exchangeType = exchangeType;
    }

    protected void connect() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);
        factory.setRequestedHeartbeat(60);
        factory.setConnectionTimeout(30000);
        factory.setAutomaticRecoveryEnabled(true);
        factory.setTopologyRecoveryEnabled(true);
        if (username != null && password != null) {
            factory.setUsername(username);
            factory.setPassword(password);
        }
        this.connection = factory.newConnection();
    }

    public void disconnect() {
        try {
            if (this.connection != null)
                connection.close();
            this.connection = null;
        } catch (Exception e) {
            logger.error("Error disconnecting from rabbitMq", e);
        }
    }

    public RabbitMqClient declareExchange(String exchangeName, BuiltinExchangeType type) {
        try {
            createChannel().exchangeDeclare(exchangeName, type);
            return this;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Channel createChannel() throws IOException, TimeoutException {
        if (this.connection == null || !this.connection.isOpen())
            connect();
        Channel channel = connection.createChannel();
        return channel;
    }

    public void setupExchange() throws IOException, TimeoutException {
        int i = 0;
        Exception exceptionReference = null;
        while (i++ < 2) {
            try {
                channel = this.createChannel();
                channel.addShutdownListener(new ShutdownListener() {
                    @Override
                    public void shutdownCompleted(ShutdownSignalException cause) {
                        if (cause.isInitiatedByApplication())
                            return;
                        logger.error("Channel shutdown", cause);
                    }
                });
                return;
            } catch (Exception e) {
                exceptionReference = e;
                logger.error("Error setting up exchange");
                try {
                    Thread.currentThread().sleep(5000);
                } catch (Exception e1) {

                }
            }
        }
        throw new RuntimeException(exceptionReference);

    }
}
