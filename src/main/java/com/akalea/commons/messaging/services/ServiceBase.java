package com.akalea.commons.messaging.services;

public abstract class ServiceBase implements MessagingService {

    protected MessagingServiceCredentials credentials;

    public MessagingServiceCredentials getCredentials() {
        return credentials;
    }

    public ServiceBase setCredentials(MessagingServiceCredentials credentials) {
        this.credentials = credentials;
        return this;
    }

}
