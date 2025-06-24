package com.david.common.dto.follow;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FollowedEventPayload implements Serializable {

    private String id;

    private String followerId;

    private String followedId;

    private Long createdAt;
}
