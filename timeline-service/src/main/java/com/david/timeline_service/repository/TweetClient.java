package com.david.timeline_service.repository;

import com.david.common.dto.FeignApiResponse;
import com.david.common.dto.tweet.TweetResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "tweet-service", url = "${app.services.tweet-service.url}")
public interface TweetClient {

    @GetMapping("/api/v1/users/{userId}/tweets")
    FeignApiResponse<List<TweetResponse>> getPublicTweets(
            @PathVariable("userId") String userId,
            @RequestParam(name = "page") int page,
            @RequestParam(name = "size") int size,
            @RequestParam(name = "sortBy") String sortBy
    );

    @GetMapping("/api/v1/tweets/batch")
    FeignApiResponse<List<TweetResponse>> getTweetsByIds(
            @RequestParam("ids") List<String> ids
    );
}
