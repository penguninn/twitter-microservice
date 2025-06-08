package com.david.tweet_service.dto.response;

import com.david.tweet_service.entity.Stats;
import com.david.tweet_service.enums.Visibility;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TweetResponse {

    private String id;

    private String userId;

    private String content;

    private List<MediaResponse> mediaItems;

    private List<String> hashtags;

    private Stats stats;

    private Visibility visibility;

    private List<String> likedBy;

    private long createdAt;

    private long updatedAt;
}
