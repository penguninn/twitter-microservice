package com.david.search_service.dto.response;

import com.david.common.dto.media.MediaResponse;
import com.david.common.dto.tweet.StatsResponse;
import com.david.common.enums.Visibility;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TweetSearchResponseDTO {

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
