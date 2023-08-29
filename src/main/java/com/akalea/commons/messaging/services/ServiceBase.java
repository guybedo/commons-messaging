package com.akalea.commons.messaging.services;

public abstract class ServiceBase implements MessagingService {

    protected MessagingServiceCredentials credentials;
    protected boolean                     connected;

    public MessagingServiceCredentials getCredentials() {
        return credentials;
    }

    public ServiceBase setCredentials(MessagingServiceCredentials credentials) {
        this.credentials = credentials;
        return this;
    }

    public boolean isConnected() {
        return connected;
    }

    public ServiceBase setConnected(boolean connected) {
        this.connected = connected;
        return this;
    }

}
