package com.akalea.commons.messaging.services.rabbit;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.Recoverable;
import com.rabbitmq.client.RecoveryListener;

public class RabbitMqConsumer extends RabbitMqClient {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private String   queueName;
    private boolean  durable;
    private boolean  exclusive;
    private boolean  autoDelete;
    private String   routingKey;
    private Consumer consumer;
    private String   consumerTag;
    
    private Function<String, Boolean> messageCallback;
    private final AtomicBoolean isConsuming = new AtomicBoolean(false);
    private final AtomicInteger retryCount = new AtomicInteger(0);
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final ScheduledExecutorService healthMonitor = Executors.newSingleThreadScheduledExecutor();
    private static final int MAX_RETRIES = 5;
    private static final long INITIAL_RETRY_DELAY_MS = 1000;
    private static final long HEALTH_CHECK_INTERVAL_MS = 30000; // 30 seconds
    private volatile long lastMessageTime = System.currentTimeMillis();

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
        
        setupRecoveryListener();
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
        setupRecoveryListener();
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
        this.messageCallback = messageCallback;
        this.isConsuming.set(true);
        this.retryCount.set(0);
        this.lastMessageTime = System.currentTimeMillis();
        startHealthMonitoring();
        attemptToStartConsuming();
    }
    
    private void attemptToStartConsuming() {
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
                        lastMessageTime = System.currentTimeMillis(); // Update last message time
                        messageCallback.apply(message);
                    } catch (Exception e) {
                        log.error("Error while consuming message ", e);
                    }
                }
            };
            consumerTag = channel.basicConsume(queueName, true, consumer);
            retryCount.set(0); // Reset retry count on successful connection
            log.info("Successfully started consuming messages from queue: {}", queueName);
        } catch (Exception e) {
            log.error("Error while consuming messages ", e);
            scheduleRetry();
        }
    }
    
    private void scheduleRetry() {
        if (!isConsuming.get()) {
            return; // Stop retrying if consuming was explicitly stopped
        }
        
        int currentRetry = retryCount.incrementAndGet();
        if (currentRetry > MAX_RETRIES) {
            log.error("Max retries ({}) exceeded. Stopping consumer for queue: {}", MAX_RETRIES, queueName);
            isConsuming.set(false);
            return;
        }
        
        long delay = INITIAL_RETRY_DELAY_MS * (long) Math.pow(2, currentRetry - 1);
        log.warn("Scheduling retry {} after {} ms for queue: {}", currentRetry, delay, queueName);
        
        scheduler.schedule(() -> {
            if (isConsuming.get()) {
                attemptToStartConsuming();
            }
        }, delay, TimeUnit.MILLISECONDS);
    }
    
    private void setupRecoveryListener() {
        setRecoveryListener(new RecoveryListener() {
            @Override
            public void handleRecovery(Recoverable recoverable) {
                log.info("Connection recovered, restarting consumer for queue: {}", queueName);
                if (isConsuming.get() && messageCallback != null) {
                    scheduler.schedule(() -> attemptToStartConsuming(), 100, TimeUnit.MILLISECONDS);
                }
            }
            
            @Override
            public void handleRecoveryStarted(Recoverable recoverable) {
                log.info("Recovery started for queue: {}", queueName);
            }
        });
    }
    
    private void startHealthMonitoring() {
        healthMonitor.scheduleWithFixedDelay(() -> {
            if (!isConsuming.get()) {
                return;
            }
            
            try {
                // Check if connection and channel are alive
                if (connection == null || !connection.isOpen() || 
                    channel == null || !channel.isOpen()) {
                    log.warn("Connection or channel is not open, triggering reconnection for queue: {}", queueName);
                    scheduleRetry();
                    return;
                }
                
                // Check if consumer is still registered
                if (consumer != null && channel.isOpen()) {
                    try {
                        // Try to get queue info as a basic health check
                        channel.queueDeclarePassive(queueName);
                    } catch (IOException e) {
                        log.warn("Queue health check failed, restarting consumer for queue: {}", queueName);
                        scheduleRetry();
                    }
                }
            } catch (Exception e) {
                log.error("Health check failed for queue: " + queueName, e);
                scheduleRetry();
            }
        }, HEALTH_CHECK_INTERVAL_MS, HEALTH_CHECK_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }
    
    public void stopConsuming() {
        isConsuming.set(false);
        try {
            if (channel != null && channel.isOpen() && consumerTag != null) {
                channel.basicCancel(consumerTag);
            }
        } catch (Exception e) {
            log.error("Error stopping consumer", e);
        }
    }
    
    public void shutdown() {
        stopConsuming();
        
        // Shutdown schedulers
        scheduler.shutdown();
        healthMonitor.shutdown();
        
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
            if (!healthMonitor.awaitTermination(5, TimeUnit.SECONDS)) {
                healthMonitor.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            healthMonitor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        disconnect();
    }
    
    public boolean isHealthy() {
        return isConsuming.get() && 
               connection != null && connection.isOpen() &&
               channel != null && channel.isOpen();
    }
    
    public int getRetryCount() {
        return retryCount.get();
    }
    
    public long getLastMessageTime() {
        return lastMessageTime;
    }
}
