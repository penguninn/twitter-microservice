package com.david.tweet_service.controller;

import com.david.common.dto.ApiResponse;
import com.david.common.dto.tweet.TweetResponse;
import com.david.tweet_service.dto.request.TweetRequest;
import com.david.tweet_service.service.TweetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class TweetController {

    private final TweetService tweetService;

    @GetMapping("/api/v1/tweets/{tweetId}")
    public ApiResponse<?> getTweetById(@PathVariable("tweetId") String tweetId) {
        log.info("TweetController::getTweetById - Execution started for tweetId: {}", tweetId);
        TweetResponse tweetResponse = tweetService.getTweetById(tweetId);
        log.info("TweetController::getTweetById - Execution ended for tweetId: {}", tweetId);
        return new ApiResponse<>(HttpStatus.OK, "Fetched tweet successfully", tweetResponse);
    }

    @GetMapping("/api/v1/tweets/batch")
    public ApiResponse<?> getTweetsByIds(
            @RequestParam(name = "ids") List<String> ids
    ) {
        log.info("TweetController::getTweetsByIds - Execution started for tweetIds: {}", ids);
        var tweetResponses = tweetService.getTweetsByIds(ids);
        log.info("TweetController::getTweetsByIds - Execution ended");
        return new ApiResponse<>(HttpStatus.OK, "Fetched tweets successfully", tweetResponses);
    }

    @GetMapping("/api/v1/tweets/me")
    public ApiResponse<?> getMyTweets(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "createdAt,desc") String sortBy
    ) {
        log.info("TweetController::getMyTweets - Execution started");
        var tweetResponses = tweetService.getMyTweets(page, size, sortBy);
        log.info("TweetController::getMyTweets - Execution ended");
        return new ApiResponse<>(HttpStatus.OK, "Fetched my tweets successfully", tweetResponses);
    }

    @GetMapping("/api/v1/users/{userId}/tweets")
    public ApiResponse<?> getPublicTweets(
            @PathVariable(name = "userId") String userId,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "createdAt,desc") String sortBy
    ) {
        log.info("TweetController::getPublicTweets - Execution started for userId: {}", userId);
        var tweetResponses = tweetService.getPublicTweets(userId, page, size, sortBy);
        log.info("TweetController::getPublicTweets - Execution ended");
        return new ApiResponse<>(HttpStatus.OK, "Fetched tweets successfully", tweetResponses);
    }

    @PostMapping("/api/v1/tweets")
    public ApiResponse<?> createTweet(
            @Valid @RequestPart("tweetData") TweetRequest request,
            @RequestPart(value = "mediaFiles", required = false) MultipartFile[] mediaFiles
    ) {
        log.info("TweetController::createTweet - Execution started");
        TweetResponse tweetResponse = tweetService.createTweet(request, mediaFiles);
        log.info("TweetController::createTweet - Execution ended");
        return new ApiResponse<>(HttpStatus.CREATED, "Created tweet successfully", tweetResponse);
    }

    @PostMapping("/api/v1/tweets/{tweetId}/likes")
    public ApiResponse<?> likeTweet(
            @PathVariable("tweetId") String tweetId) {
        log.info("TweetController::likeTweet - Execution started for tweetId: {}", tweetId);
        TweetResponse tweetResponse = tweetService.likeTweet(tweetId);
        log.info("TweetController::likeTweet - Execution ended for tweetId: {}", tweetId);
        return new ApiResponse<>(HttpStatus.OK, "Like tweet successfully", tweetResponse);
    }

    @DeleteMapping("/api/v1/tweets/{tweetId}/likes")
    public ApiResponse<?> unlikeTweet(
            @PathVariable("tweetId") String tweetId) {
        log.info("TweetController::unlikeTweet - Execution started for tweetId: {}", tweetId);
        TweetResponse tweetResponse = tweetService.unlikeTweet(tweetId);
        log.info("TweetController::unlikeTweet - Execution ended for tweetId: {}", tweetId);
        return new ApiResponse<>(HttpStatus.OK, "Unlike tweet successfully", tweetResponse);
    }

    @DeleteMapping("/api/v1/tweets/{tweetId}")
    public ApiResponse<?> deleteTweet(
            @PathVariable("tweetId") String tweetId) {
        log.info("TweetController::deleteTweet - Execution started for tweetId: {}", tweetId);
        tweetService.deleteTweet(tweetId);
        log.info("TweetController::deleteTweet - Execution ended for tweetId: {}", tweetId);
        return new ApiResponse<>(HttpStatus.NO_CONTENT, "Deleted tweet successfully");
    }
}
