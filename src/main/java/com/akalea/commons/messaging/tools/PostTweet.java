package com.akalea.commons.messaging.tools;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.UUID;

import org.apache.commons.io.IOUtils;

import com.akalea.commons.messaging.message.Msg;
import com.akalea.commons.messaging.message.MsgMedia;
import com.akalea.commons.messaging.message.MsgMedia.MediaType;
import com.akalea.commons.messaging.services.twitter.TwitterService;
import com.akalea.commons.messaging.services.twitter.TwitterTokens;

public class PostTweet {

    public static void main(String[] args) throws MalformedURLException, IOException {
        TwitterService service =
            (TwitterService) new TwitterService()
                .setCredentials(
                    new TwitterTokens()
                        .setAccessToken("1660759852166311936-H3HgEvGOPoNInHGzVJuE4lixdShyMC")
                        .setAccessTokenSecret("EKc3sxqe5O8LgTXhTPH2Y3gB2DcLFuq6PhzNSOYl4rjLn")
                        .setApiKey("Ssp8SDS6zPQ6uhsvVuNDMCzp2")
                        .setApiSecret("2k3wacK2WJKDGAlcGvt7dJ1QV5ovxIrIZyRw6i7fSTbBSPlafi"))
                .connect();

        URLConnection connection =
            new URL(
                "https://edgefound.xyz/public/patterns/Support+Break+%26+Confirm/occurences/LTC-USDT/tf5m/81adc364-1829-4161-915e-e86a06dd9488/images/main.png")
                    .openConnection();
        connection
            .setRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
        byte[] data = IOUtils.toByteArray(connection.getInputStream());
        service.sendMessage(
            new Msg()
                .setContent(UUID.randomUUID().toString())
                .setMedia(
                    new MsgMedia()
                        .setFilename("test.png")
                        .setData(data)
                        .setType(MediaType.img)));
    }
}
