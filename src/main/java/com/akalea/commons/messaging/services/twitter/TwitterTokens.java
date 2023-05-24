package com.akalea.commons.messaging.services.twitter;

import com.akalea.commons.messaging.services.MessagingServiceCredentials;

public class TwitterTokens extends MessagingServiceCredentials {

    private String accessToken;
    private String accessTokenSecret;

    public String getApiKey() {
        return super.getKey();
    }

    public TwitterTokens setApiKey(String consumerKey) {
        super.setKey(consumerKey);
        return this;
    }

    public String getApiSecret() {
        return super.getToken();
    }

    public TwitterTokens setApiSecret(String consumerSecret) {
        super.setToken(consumerSecret);
        return this;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public TwitterTokens setAccessToken(String accessToken) {
        this.accessToken = accessToken;
        return this;
    }

    public String getAccessTokenSecret() {
        return accessTokenSecret;
    }

    public TwitterTokens setAccessTokenSecret(String accessTokenSecret) {
        this.accessTokenSecret = accessTokenSecret;
        return this;
    }

}
