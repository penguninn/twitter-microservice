package com.david.common.dto.follow;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FollowResponse implements Serializable {

    private String id;

    private String followerId;

    private String followedId;

    private Long createdAt;
}
