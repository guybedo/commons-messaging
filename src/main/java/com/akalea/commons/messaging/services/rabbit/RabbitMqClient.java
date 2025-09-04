package com.akalea.commons.messaging.services.rabbit;

import com.rabbitmq.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RabbitMqClient {

    private static final Logger logger = LoggerFactory.getLogger(RabbitMqClient.class);

    protected final String host;
    protected final int    port;
    protected final String username;
    protected final String password;
    protected final String exchangeName;
    protected final String exchangeType;

    // These settings will be applied to each new channel
    protected Integer prefetchCount;
    protected boolean confirmPublish;

    protected volatile Connection connection;
    protected RecoveryListener    recoveryListener;
    private final Object          connectionLock = new Object();

    public RabbitMqClient(
        String host,
        int port,
        String username,
        String password,
        String exchangeName,
        String exchangeType) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.exchangeName = exchangeName;
        this.exchangeType = exchangeType;
    }

    /**
     * Ensures a connection is established. This method is thread-safe.
     */
    protected void connect() throws IOException, TimeoutException {
        // Double-checked locking to prevent multiple connections under load
        if (connection == null || !connection.isOpen()) {
            synchronized (connectionLock) {
                if (connection == null || !connection.isOpen()) {
                    logger.info("Connecting to RabbitMQ at {}:{}", host, port);
                    ConnectionFactory factory = new ConnectionFactory();
                    factory.setHost(host);
                    factory.setPort(port);
                    factory.setRequestedHeartbeat(60);
                    factory.setConnectionTimeout(30000);

                    // These are crucial for robust connections
                    factory.setAutomaticRecoveryEnabled(true);
                    factory.setTopologyRecoveryEnabled(true);

                    if (username != null && password != null) {
                        factory.setUsername(username);
                        factory.setPassword(password);
                    }

                    this.connection = factory.newConnection();
                    logger.info("Successfully connected to RabbitMQ.");

                    if (recoveryListener != null && this.connection instanceof Recoverable) {
                        ((Recoverable) this.connection).addRecoveryListener(recoveryListener);
                    }
                }
            }
        }
    }

    public void disconnect() {
        try {
            synchronized (connectionLock) {
                if (this.connection != null && this.connection.isOpen()) {
                    logger.info("Disconnecting from RabbitMQ.");
                    connection.close();
                }
                this.connection = null;
            }
        } catch (IOException e) {
            logger.error("Error while disconnecting from RabbitMQ", e);
        }
    }

    /**
     * Creates a new, configured channel. The caller is responsible for closing
     * it. This is intended for use by subclasses that need long-lived channels
     * (like a consumer).
     */
    protected Channel createChannel() throws IOException, TimeoutException {
        connect(); // Ensure connection exists

        Channel channel = this.connection.createChannel();
        if (prefetchCount != null) {
            channel.basicQos(prefetchCount);
        }
        if (confirmPublish) {
            channel.confirmSelect();
        }

        // Add a listener to log unexpected channel closures
        channel.addShutdownListener(cause -> {
            if (!cause.isInitiatedByApplication()) {
                logger.error("Channel shutdown unexpectedly. Reason: {}", cause.getReason(), cause);
            }
        });
        return channel;
    }

    /**
     * Safely executes an action using a temporary channel that is automatically
     * closed. This is the recommended way for performing short-lived operations
     * like publishing.
     *
     * @param action
     *            The operation to perform on the channel.
     */
    public void execute(ChannelCallback action) throws IOException, TimeoutException {
        // try-with-resources ensures the channel is always closed, preventing
        // leaks.
        try (Channel channel = createChannel()) {
            action.execute(channel);
        }
    }

    /**
     * Declares the exchange using a safe, temporary channel.
     */
    public void declareExchange() throws IOException, TimeoutException {
        execute(channel -> channel.exchangeDeclare(exchangeName, exchangeType, true));
    }

    // The old setupExchange() method is removed entirely.

    public void setRecoveryListener(RecoveryListener listener) {
        this.recoveryListener = listener;
    }

    /**
     * A functional interface for channel operations.
     */
    @FunctionalInterface
    public interface ChannelCallback {
        void execute(Channel channel) throws IOException;
    }
}