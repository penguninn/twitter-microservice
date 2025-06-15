package com.david.comment_service.repository;

import com.david.common.dto.FeignApiResponse;
import com.david.common.dto.tweet.TweetResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "tweet-service", url = "${app.services.tweet-service.url}")
public interface TweetClient {

    @GetMapping(value = "/api/v1/tweets/{tweetId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    FeignApiResponse<TweetResponse> getTweetById(@PathVariable("tweetId") String tweetId);
}
