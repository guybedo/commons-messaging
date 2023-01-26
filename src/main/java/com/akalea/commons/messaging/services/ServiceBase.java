package com.akalea.commons.messaging.services;

public abstract class ServiceBase implements MessageService {

    protected MessageServiceCredentials credentials;

    public MessageServiceCredentials getCredentials() {
        return credentials;
    }

    public ServiceBase setCredentials(MessageServiceCredentials credentials) {
        this.credentials = credentials;
        return this;
    }

}
