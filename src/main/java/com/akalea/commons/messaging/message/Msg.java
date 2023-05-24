package com.akalea.commons.messaging.message;

import java.util.List;

import com.google.common.collect.Lists;

public class Msg {

    private String id;

    private User user;
    private User recipient;

    private Msg replyTo;

    private String   content;
    private MsgMedia media;

    private List<Msg> thread = Lists.newArrayList();

    public List<Msg> getThread() {
        return thread;
    }

    public Msg setThread(Msg... thread) {
        return setThread(Lists.newArrayList(thread));
    }

    public Msg setThread(List<Msg> thread) {
        this.thread = thread;
        return this;
    }

    public MsgMedia getMedia() {
        return media;
    }

    public Msg setMedia(MsgMedia media) {
        this.media = media;
        return this;

    }

    public String getId() {
        return id;
    }

    public Msg setId(String id) {
        this.id = id;
        return this;
    }

    public Msg getReplyTo() {
        return replyTo;
    }

    public Msg setReplyTo(Msg replyTo) {
        this.replyTo = replyTo;
        return this;
    }

    public User getUser() {
        return user;
    }

    public Msg setUser(User userId) {
        this.user = userId;
        return this;
    }

    public User getRecipient() {
        return recipient;
    }

    public Msg setRecipient(User recipientId) {
        this.recipient = recipientId;
        return this;
    }

    public String getContent() {
        return content;
    }

    public Msg setContent(String content) {
        this.content = content;
        return this;
    }

}
