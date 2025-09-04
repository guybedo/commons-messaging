package com.akalea.commons.messaging.services.rabbit;

import com.rabbitmq.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class RabbitMqConsumer extends RabbitMqClient {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private static final int  MAX_RETRIES              = 5;
    private static final long INITIAL_RETRY_DELAY_MSEC = 1000;

    private final String  queueName;
    private final boolean durable;
    private final boolean exclusive;
    private final boolean autoDelete;
    private final String  routingKey;

    // --- CHANGE --- The consumer now manages its own channel instance.
    // It's volatile because it can be reassigned by the recovery listener
    // thread.
    private volatile Channel channel;
    private String           consumerTag;

    private Function<String, Boolean>      messageCallback;
    private final AtomicBoolean            isConsuming = new AtomicBoolean(false);
    private final AtomicInteger            retryCount  = new AtomicInteger(0);
    private final ScheduledExecutorService scheduler   =
        Executors.newSingleThreadScheduledExecutor();

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

        if (exclusive) {
            // Create a unique queue name for exclusive consumers
            this.queueName =
                String.format("%s-%s", queueName, UUID.randomUUID().toString().substring(0, 8));
        } else {
            this.queueName = queueName;
        }

        // The recovery listener is key to resilience
        setupRecoveryListener();
    }

    public void startConsuming(Function<String, Boolean> messageCallback) {
        this.messageCallback = messageCallback;
        if (!isConsuming.compareAndSet(false, true)) {
            log.warn("Consumer is already running for queue [{}]", queueName);
            return;
        }
        this.retryCount.set(0);
        attemptToStartConsuming();
    }

    private void attemptToStartConsuming() {
        if (!isConsuming.get()) {
            return; // Guard against starting if stop was called.
        }

        try {
            // --- CHANGE --- The consumer now creates its own channel and sets
            // up the queue topology.
            // The old `setupQueue()` method is no longer needed.
            log.info("Attempting to set up consumer for queue [{}]", queueName);
            this.channel = createChannel(); // Request a new channel from the
                                            // parent

            // Declare exchange to ensure it exists (idempotent)
            this.channel.exchangeDeclare(exchangeName, exchangeType, true);

            // Declare and bind the queue
            this.channel.queueDeclare(queueName, durable, exclusive, autoDelete, new HashMap<>());
            this.channel.queueBind(queueName, exchangeName, routingKey);
            log.info("Queue [{}] is declared and bound to exchange [{}]", queueName, exchangeName);

            Consumer consumer = new DefaultConsumer(this.channel) {
                @Override
                public void handleDelivery(
                    String consumerTag,
                    Envelope envelope,
                    AMQP.BasicProperties properties,
                    byte[] body)
                    throws IOException {
                    long deliveryTag = envelope.getDeliveryTag();
                    try {
                        String message = new String(body, "UTF-8");
                        if (messageCallback.apply(message)) {
                            channel.basicAck(deliveryTag, false);
                        } else {
                            log.warn(
                                "Message processing failed, nacking and re-queuing message from queue [{}]",
                                queueName);
                            channel.basicNack(deliveryTag, false, true); // Re-queue
                        }
                    } catch (Exception e) {
                        log.error(
                            "Unhandled exception processing message from queue [{}]. Nacking without re-queue.",
                            queueName,
                            e);
                        channel.basicNack(deliveryTag, false, false); // Do not
                                                                      // re-queue
                                                                      // (avoids
                                                                      // poison
                                                                      // messages)
                    }
                }
            };

            // Start consuming messages
            this.consumerTag = this.channel.basicConsume(queueName, false, consumer); // autoAck
                                                                                      // =
                                                                                      // false
            retryCount.set(0); // Reset on success
            log.info("Successfully started consuming messages from queue [{}]", queueName);

        } catch (Exception e) {
            log.error(
                "Failed to start consumer for queue [{}]. Error: {}",
                queueName,
                e.getMessage(),
                e);
            closeChannel(); // Clean up the failed channel
            scheduleRetry();
        }
    }

    private void scheduleRetry() {
        if (!isConsuming.get())
            return;

        int currentRetry = retryCount.incrementAndGet();
        if (currentRetry > MAX_RETRIES) {
            log.error(
                "Max retries ({}) exceeded. Stopping consumer for queue [{}]",
                MAX_RETRIES,
                queueName);
            stopConsuming();
            return;
        }

        long delay = INITIAL_RETRY_DELAY_MSEC * (long) Math.pow(2, currentRetry - 1);
        log.warn("Scheduling retry {} in {} ms for queue [{}]", currentRetry, delay, queueName);
        scheduler.schedule(this::attemptToStartConsuming, delay, TimeUnit.MILLISECONDS);
    }

    private void setupRecoveryListener() {
        setRecoveryListener(new RecoveryListener() {
            @Override
            public void handleRecovery(Recoverable recoverable) {
                log.info(
                    "Connection recovered for queue [{}]. Re-initializing consumer.",
                    queueName);
                retryCount.set(0);
                attemptToStartConsuming(); // Re-run the setup logic on the new
                                           // connection
            }

            @Override
            public void handleRecoveryStarted(Recoverable recoverable) {
                log.info("Connection recovery started for queue [{}]", queueName);
            }
        });
    }

    public void stopConsuming() {
        if (!isConsuming.compareAndSet(true, false)) {
            return;
        }
        log.info("Stopping consumer for queue [{}]", queueName);
        closeChannel();
    }

    private void closeChannel() {
        // --- CHANGE --- Helper method to safely close the consumer's channel
        try {
            if (this.channel != null && this.channel.isOpen()) {
                if (consumerTag != null) {
                    this.channel.basicCancel(consumerTag);
                }
                this.channel.close();
            }
        } catch (Exception e) {
            log.error("Error closing channel for queue [{}]: {}", queueName, e.getMessage());
        } finally {
            this.channel = null;
            this.consumerTag = null;
        }
    }

    public void shutdown() {
        stopConsuming();
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        disconnect(); // Disconnects the parent connection
    }

    public boolean isHealthy() {
        // --- CHANGE --- Checks the consumer's own channel instance
        return isConsuming.get()
            && connection != null
            && connection.isOpen()
            && this.channel != null
            && this.channel.isOpen();
    }
}