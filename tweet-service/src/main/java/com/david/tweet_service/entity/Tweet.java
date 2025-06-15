package com.david.tweet_service.entity;

import com.david.common.enums.Visibility;
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

    @Builder.Default
    private List<Media> mediaItems = new ArrayList<>();

    @Builder.Default
    private List<String> hashtags = new ArrayList<>();

    @Builder.Default
    private Stats stats = new Stats();

    @Builder.Default
    private List<String> likedBy = new ArrayList<>();

    private Visibility visibility;

    @CreatedDate
    private long createdAt;

    @LastModifiedDate
    private long updatedAt;

}
