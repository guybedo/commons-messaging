package com.akalea.commons.messaging.services.rabbit;

public class RabbitMqConsumerConfiguration {
    private String  host;
    private int     port;
    private String  username;
    private String  password;
    private String  exchangeName;
    private String  exchangeType;
    private String  queueName;
    private boolean durable;
    private boolean exclusive;
    private boolean autoDelete;
    private String  routingKey;

    private Integer prefetchCount;

    private boolean confirmPublish;
    private long    confirmPublishDelayMsec;

    public static RabbitMqConsumerConfiguration of(RabbitMqConsumerConfiguration c) {
        return new RabbitMqConsumerConfiguration().setPrefetchCount(c.getPrefetchCount())
            .setConfirmPublish(c.isConfirmPublish())
            .setConfirmPublishDelayMsec(c.getConfirmPublishDelayMsec())
            .setHost(c.getHost())
            .setPort(c.getPort())
            .setUsername(c.getUsername())
            .setPassword(c.getPassword())
            .setExchangeName(c.getExchangeName())
            .setExchangeType(c.getExchangeType())
            .setQueueName(c.getQueueName())
            .setDurable(c.isDurable())
            .setExclusive(c.isExclusive())
            .setAutoDelete(c.isAutoDelete())
            .setRoutingKey(c.getRoutingKey());
    }

    public Integer getPrefetchCount() {
        return prefetchCount;
    }

    public RabbitMqConsumerConfiguration setPrefetchCount(Integer prefetchCount) {
        this.prefetchCount = prefetchCount;
        return this;
    }

    public boolean isConfirmPublish() {
        return confirmPublish;
    }

    public RabbitMqConsumerConfiguration setConfirmPublish(boolean confirmPublish) {
        this.confirmPublish = confirmPublish;
        return this;
    }

    public long getConfirmPublishDelayMsec() {
        return confirmPublishDelayMsec;
    }

    public RabbitMqConsumerConfiguration setConfirmPublishDelayMsec(long confirmPublishDelayMsec) {
        this.confirmPublishDelayMsec = confirmPublishDelayMsec;
        return this;
    }

    public String getHost() {
        return host;
    }

    public RabbitMqConsumerConfiguration setHost(String host) {
        this.host = host;
        return this;
    }

    public int getPort() {
        return port;
    }

    public RabbitMqConsumerConfiguration setPort(int port) {
        this.port = port;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public RabbitMqConsumerConfiguration setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public RabbitMqConsumerConfiguration setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getExchangeName() {
        return exchangeName;
    }

    public RabbitMqConsumerConfiguration setExchangeName(String exchangeName) {
        this.exchangeName = exchangeName;
        return this;
    }

    public String getExchangeType() {
        return exchangeType;
    }

    public RabbitMqConsumerConfiguration setExchangeType(String exchangeType) {
        this.exchangeType = exchangeType;
        return this;
    }

    public String getQueueName() {
        return queueName;
    }

    public RabbitMqConsumerConfiguration setQueueName(String queueName) {
        this.queueName = queueName;
        return this;
    }

    public boolean isDurable() {
        return durable;
    }

    public RabbitMqConsumerConfiguration setDurable(boolean durable) {
        this.durable = durable;
        return this;
    }

    public boolean isExclusive() {
        return exclusive;
    }

    public RabbitMqConsumerConfiguration setExclusive(boolean exclusive) {
        this.exclusive = exclusive;
        return this;
    }

    public boolean isAutoDelete() {
        return autoDelete;
    }

    public RabbitMqConsumerConfiguration setAutoDelete(boolean autoDelete) {
        this.autoDelete = autoDelete;
        return this;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public RabbitMqConsumerConfiguration setRoutingKey(String routingKey) {
        this.routingKey = routingKey;
        return this;
    }

}