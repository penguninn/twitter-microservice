package com.david.common.dto.tweet;

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
public class TweetResponse implements Serializable {

    private String id;

    private String userId;

    private String content;

    private List<MediaResponse> mediaItems;

    private List<String> hashtags;

    private StatsResponse statsResponse;

    private Visibility visibility;

    private List<String> likedBy;

    private long createdAt;

    private long updatedAt;
}
