package com.david.tweet_service.dto.request;

import com.david.tweet_service.enums.Visibility;
import lombok.Builder;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;

@Getter
@Builder
public class TweetRequest {

    private String content;

    private List<String> hashtags;

    private Visibility visibility;
}
