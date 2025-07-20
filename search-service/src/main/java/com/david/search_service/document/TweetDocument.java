package com.david.search_service.document;

import com.david.common.dto.media.MediaResponse;
import com.david.common.dto.tweet.StatsResponse;
import com.david.common.enums.Visibility;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.util.List;

@Data
@Document(indexName = "tweets")
public class TweetDocument {

    @Id
    private String id;

    private String tweetId;

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
