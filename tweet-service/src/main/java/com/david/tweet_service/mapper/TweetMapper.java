package com.david.tweet_service.mapper;

import com.david.tweet_service.dto.request.TweetRequest;
import com.david.tweet_service.dto.response.TweetResponse;
import com.david.tweet_service.entity.Tweet;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {MediaMapper.class})
public interface TweetMapper {
    TweetResponse toDto(Tweet tweet);
    Tweet toEntity(TweetRequest tweetRequest);
}
