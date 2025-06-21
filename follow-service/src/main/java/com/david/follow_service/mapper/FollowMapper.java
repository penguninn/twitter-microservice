package com.david.follow_service.mapper;

import com.david.common.dto.follow.FollowResponse;
import com.david.follow_service.entity.UserFollow;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FollowMapper {

    FollowResponse toDto(UserFollow userFollow);

    UserFollow toEntity(String followerId, String followedId);
}
