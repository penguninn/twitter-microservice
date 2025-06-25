package com.david.common.dto.tweet;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TweetCreatedEventPayload implements Serializable {

    private String tweetId;

    private String userId;

    private long createdAt;
}
