package com.david.tweet_service.dto.event;

import com.david.common.dto.media.MediaResponse;
import com.david.common.enums.Visibility;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TweetCreatedEvent implements Serializable {

    private String tweetId;

    private String userId;

    private String content;

    private List<MediaResponse> mediaList;

    private List<String> hashtags;

    private Visibility visibility;

    private long createdAt;
}
