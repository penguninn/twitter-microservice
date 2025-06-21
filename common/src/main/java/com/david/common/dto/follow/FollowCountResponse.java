package com.david.common.dto.follow;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FollowCountResponse implements Serializable {

    private Long followingCount;

    private Long followerCount;
}
