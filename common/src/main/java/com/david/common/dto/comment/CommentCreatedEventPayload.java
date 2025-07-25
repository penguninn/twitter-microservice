package com.david.common.dto.comment;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentCreatedEventPayload implements Serializable {

    private String commentId;

    private String tweetId;

    private String userId;

    private String parentId;

    private Long createdAt;
}
