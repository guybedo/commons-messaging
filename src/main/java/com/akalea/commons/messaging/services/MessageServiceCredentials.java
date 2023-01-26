package com.akalea.commons.messaging.services;

public class MessageServiceCredentials {

    private String uuid;
    private String key;
    private String token;

    public String getUuid() {
        return uuid;
    }

    public MessageServiceCredentials setUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public String getKey() {
        return key;
    }

    public MessageServiceCredentials setKey(String key) {
        this.key = key;
        return this;
    }

    public String getToken() {
        return token;
    }

    public MessageServiceCredentials setToken(String token) {
        this.token = token;
        return this;
    }

}
