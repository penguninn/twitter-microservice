package com.david.profile_service.mapper;

import com.david.common.dto.profile.ProfileCreatedEventPayload;
import com.david.common.dto.profile.ProfileResponse;
import com.david.profile_service.entity.Profile;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProfileMapper {

    ProfileResponse toDto(Profile profile);

    Profile toEntity(ProfileCreatedEventPayload profileCreatedEventPayload);
}
