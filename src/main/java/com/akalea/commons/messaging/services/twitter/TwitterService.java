package com.akalea.commons.messaging.services.twitter;

import java.util.List;

import com.akalea.commons.messaging.message.Msg;
import com.akalea.commons.messaging.message.MsgMedia;
import com.akalea.commons.messaging.message.MsgMedia.MediaType;
import com.akalea.commons.messaging.message.MsgReceipt;
import com.akalea.commons.messaging.services.ServiceBase;
import com.google.common.collect.Lists;

import io.github.redouane59.twitter.TwitterClient;
import io.github.redouane59.twitter.dto.tweet.MediaCategory;
import io.github.redouane59.twitter.dto.tweet.Tweet;
import io.github.redouane59.twitter.dto.tweet.TweetParameters;
import io.github.redouane59.twitter.dto.tweet.TweetParameters.Media;
import io.github.redouane59.twitter.dto.tweet.TweetParameters.Reply;
import io.github.redouane59.twitter.dto.tweet.TweetParameters.TweetParametersBuilder;
import io.github.redouane59.twitter.dto.tweet.UploadMediaResponse;
import io.github.redouane59.twitter.signature.TwitterCredentials;

public class TwitterService extends ServiceBase {

    private TwitterClient client;

    public TwitterService connect() {
        TwitterTokens credentials = (TwitterTokens) this.credentials;
        client =
            new TwitterClient(
                TwitterCredentials
                    .builder()
                    .accessToken(credentials.getAccessToken())
                    .accessTokenSecret(credentials.getAccessTokenSecret())
                    .apiKey(credentials.getApiKey())
                    .apiSecretKey(credentials.getApiSecret())
                    .build());
        return this;
    }

    public TwitterService disconnect() {
        return this;
    }

    public String uploadMedia(MsgMedia media) {
        MediaCategory category = null;
        if (MediaType.img.equals(media.getType()))
            category = MediaCategory.TWEET_IMAGE;
        else if (MediaType.gif.equals(media.getType()))
            category = MediaCategory.TWEET_GIF;
        else if (MediaType.video.equals(media.getType()))
            category = MediaCategory.TWEET_VIDEO;
        UploadMediaResponse response =
            client.uploadMedia(media.getFilename(), media.getData(), category);
        return response.getMediaId();
    }

    @Override
    public MsgReceipt sendMessage(Msg msg) {
        try {
            TweetParametersBuilder builder =
                TweetParameters
                    .builder()
                    .text(msg.getContent());
            if (msg.getMedia() != null) {
                String mediaId = uploadMedia(msg.getMedia());
                builder =
                    builder.media(
                        Media
                            .builder()
                            .mediaIds(Lists.newArrayList(mediaId))
                            .build());
            }
            TweetParameters tweetParameters = builder.build();
            Tweet tweet = client.postTweet(tweetParameters);
            MsgReceipt receipt =
                new MsgReceipt()
                    .setMessage(new Msg().setId(tweet.getId()))
                    .setSuccess(true);
            if (!msg.getThread().isEmpty()) {
                String parentMessageId = receipt.getMessage().getId();
                for (Msg reply : msg.getThread()) {
                    MsgReceipt replyReceipt = reply(parentMessageId, reply);
                    if (replyReceipt == null)
                        break;
                    parentMessageId = replyReceipt.getMessage().getId();
                }
            }
            return receipt;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public MsgReceipt reply(String parentMsgId, Msg msg) {
        try {
            TweetParametersBuilder builder =
                TweetParameters
                    .builder()
                    .text(msg.getContent())
                    .reply(Reply.builder().inReplyToTweetId(parentMsgId).build());
            if (msg.getMedia() != null) {
                String mediaId = uploadMedia(msg.getMedia());
                builder =
                    builder.media(
                        Media
                            .builder()
                            .mediaIds(Lists.newArrayList(mediaId))
                            .build());
            }
            TweetParameters tweetParameters = builder.build();
            Tweet tweet = client.postTweet(tweetParameters);
            return new MsgReceipt()
                .setMessage(new Msg().setId(tweet.getId()))
                .setSuccess(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<Msg> getNewMessages() {
        return Lists.newArrayList();
    }

}
