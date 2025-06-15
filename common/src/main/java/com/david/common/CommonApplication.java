package com.david.common;

import com.david.common.dto.tweet.TweetResponse;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CommonApplication {

    public static void main(String[] args) {
        SpringApplication.run(CommonApplication.class, args);
        TweetResponse tweetResponse = new TweetResponse();
        tweetResponse.setId("12345");
        System.out.println("CommonApplication started successfully with TweetResponse ID: " + tweetResponse.getId());
    }

}
