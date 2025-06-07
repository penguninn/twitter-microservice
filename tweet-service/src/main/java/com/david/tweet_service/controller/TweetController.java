package com.david.tweet_service.controller;

import com.david.tweet_service.dto.request.TweetRequest;
import com.david.tweet_service.dto.response.ApiResponse;
import com.david.tweet_service.dto.response.TweetResponse;
import com.david.tweet_service.service.TweetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/tweets")
public class TweetController {

    private final TweetService tweetService;

    @GetMapping("/{tweetId}")
    public ApiResponse<?> getTweetById(@PathVariable("tweetId") String tweetId) {
        log.info("TweetController::getTweetById - Execution started for tweetId: {}", tweetId);
        TweetResponse tweetResponse = tweetService.getTweetById(tweetId);
        log.info("TweetController::getTweetById - Execution ended for tweetId: {}", tweetId);
        return new ApiResponse<>(HttpStatus.OK, "Fetched tweet successfully", tweetResponse);
    }

    @GetMapping("/me/{userId}")
    public ApiResponse<?> getMyTweets(
            @PathVariable(name = "userId") String userId,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "createdAt,desc") String sortBy
    ) {
        log.info("TweetController::getMyTweets - Execution started for userId: {}", userId);
        var tweetResponses = tweetService.getMyTweets(userId, page, size, sortBy);
        log.info("TweetController::getMyTweets - Execution ended");
        return new ApiResponse<>(HttpStatus.OK, "Fetched my tweets successfully", tweetResponses);
    }

    @GetMapping("/guest/{userId}")
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

    @PostMapping("/{userId}")
    public ApiResponse<?> createTweet(
            @PathVariable(name = "userId") String userId,
            @Valid @RequestPart("tweetData") TweetRequest request,
            @RequestPart(value = "mediaFiles", required = false) MultipartFile[] mediaFiles
    ) {
        log.info("TweetController::createTweet - Execution started");
        TweetResponse tweetResponse = tweetService.createTweet(userId, request, mediaFiles);
        log.info("TweetController::createTweet - Execution ended");
        return new ApiResponse<>(HttpStatus.CREATED, "Created tweet successfully", tweetResponse);
    }

    @PatchMapping("/{tweetId}/u/{userId}/toggle-like")
    public ApiResponse<?> toggleLikeTweet(
            @PathVariable("tweetId") String tweetId,
            @PathVariable("userId") String userId
    ) {
        log.info("TweetController::toggleLikeTweet - Execution started for tweetId: {}", tweetId);
        TweetResponse tweetResponse = tweetService.toggleLikeTweet(tweetId, userId);
        log.info("TweetController::toggleLikeTweet - Execution ended for tweetId: {}", tweetId);
        return new ApiResponse<>(HttpStatus.OK, "Toggle like tweet successfully", tweetResponse);
    }

    @DeleteMapping("/{tweetId}/u/{userId}")
    public ApiResponse<?> deleteTweet(
            @PathVariable("tweetId") String tweetId,
            @PathVariable("userId") String userId
    ) {
        log.info("TweetController::deleteTweet - Execution started for tweetId: {}", tweetId);
        tweetService.deleteTweet(tweetId, userId);
        log.info("TweetController::deleteTweet - Execution ended for tweetId: {}", tweetId);
        return new ApiResponse<>(HttpStatus.NO_CONTENT, "Deleted tweet successfully");
    }
}
