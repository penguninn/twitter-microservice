package com.david.tweet_service.mapper;

import com.david.tweet_service.dto.request.TweetRequest;
import com.david.tweet_service.dto.response.MediaResponse;
import com.david.tweet_service.dto.response.TweetResponse;
import com.david.tweet_service.entity.Tweet;

public class TweetMapper {

    public static Tweet mapToEntity(TweetRequest tweet) {
        return Tweet.builder()
                .content(tweet.getContent())
                .hashtags(tweet.getHashtags())
                .visibility(tweet.getVisibility())
                .build();
    }

    public static TweetResponse mapToDto(Tweet tweet) {
        return TweetResponse.builder()
                .id(tweet.getId())
                .userId(tweet.getUserId())
                .content(tweet.getContent())
                .mediaItems(tweet.getMedia())
                .hashtags(tweet.getHashtags())
                .stats(tweet.getStats())
                .visibility(tweet.getVisibility())
                .likedBy(tweet.getLikedBy())
                .createdAt(tweet.getCreatedAt())
                .updatedAt(tweet.getUpdatedAt())
                .build();
    }
}
