package com.david.common.dto.tweet;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TweetLikedEventPayload {

    private String tweetId;

    private String userId;

    private String createdAt;
}
