package com.akalea.commons.messaging.services;

public class MessagingServiceCredentials {

    private String uuid;
    private String key;
    private String token;

    public String getUuid() {
        return uuid;
    }

    public MessagingServiceCredentials setUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public String getKey() {
        return key;
    }

    public MessagingServiceCredentials setKey(String key) {
        this.key = key;
        return this;
    }

    public String getToken() {
        return token;
    }

    public MessagingServiceCredentials setToken(String token) {
        this.token = token;
        return this;
    }

}
