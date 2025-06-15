package com.david.common.dto.comment;


import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentResponse implements Serializable {

    private String id;

    private String tweetId;

    private String userId;

    private String content;

    private String parentId;

    private Long createdAt;

    private Long updatedAt;
}
