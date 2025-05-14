package com.david.profile_service.mapper;

import com.david.profile_service.dto.request.ProfileRegisterRequest;
import com.david.profile_service.dto.response.ProfileResponse;
import com.david.profile_service.entity.Profile;

public class ProfileMapper {

    public static Profile mapToEntity(ProfileRegisterRequest request) {
        return Profile.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .dob(request.getDob())
                .build();
    }

    public static ProfileResponse mapToDto(Profile profile) {
        return ProfileResponse.builder()
                .userId(profile.getUserId())
                .username(profile.getUsername())
                .email(profile.getEmail())
                .firstName(profile.getFirstName())
                .lastName(profile.getLastName())
                .dob(profile.getDob())
                .build();
    }
}
