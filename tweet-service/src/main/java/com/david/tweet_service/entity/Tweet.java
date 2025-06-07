package com.david.tweet_service.entity;

import com.david.tweet_service.dto.response.MediaResponse;
import com.david.tweet_service.enums.Visibility;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "tweets")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Tweet {

    @Id
    private String id;

    private String userId;

    private String content;

    private List<MediaResponse> media = new ArrayList<>();

    private List<String> hashtags = new ArrayList<>();

    private Stats stats = Stats.builder()
            .likesCount(0)
            .build();

    private List<String> likedBy = new ArrayList<>();

    private Visibility visibility = Visibility.PUBLIC;

    @CreatedDate
    private long createdAt;

    @LastModifiedDate
    private long updatedAt;

}
