package com.akalea.commons.messaging.services.slack;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.akalea.commons.messaging.message.Msg;
import com.akalea.commons.messaging.message.MsgReceipt;
import com.akalea.commons.messaging.services.ServiceBase;
import com.alibaba.fastjson2.JSON;
import com.google.common.collect.ImmutableMap;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SlackService extends ServiceBase {
    private final static Logger logger = LoggerFactory.getLogger(SlackService.class);

    public static final MediaType jsonType      = MediaType.get("application/json; charset=utf-8");
    private final String          slackHooksUrl = "https://hooks.slack.com/services";
    private OkHttpClient          client        = new OkHttpClient();

    public SlackService connect() {
        return this;
    }

    public SlackService disconnect() {
        return this;
    }

    @Override
    public MsgReceipt sendMessage(Msg msg) {
        RequestBody body =
            RequestBody.create(
                JSON.toJSONString(ImmutableMap.of("text", msg.getContent())),
                jsonType);
        Request request =
            new Request.Builder()
                .url(msg.getRecipient().getUuid())
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            return new MsgReceipt()
                .setMessage(new Msg().setContent(response.body().string()))
                .setSuccess(true);
        } catch (Exception e) {
            logger.error("Error sending message", e);
            throw new RuntimeException(e);
        }

    }

    private String buildChannelHookUrl(String uuid) {
        return String.format("%s/%s", slackHooksUrl, uuid);
    }

    public List<Msg> getNewMessages() {
        throw new RuntimeException("Not implemented");
    }

}
