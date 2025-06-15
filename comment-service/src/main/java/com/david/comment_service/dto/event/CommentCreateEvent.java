package com.david.comment_service.dto.event;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentCreateEvent implements Serializable {

    private String commentId;

    private String tweetId;

    private String userId;

    private String content;

    private String parentId;

    private Long createdAt;
}
